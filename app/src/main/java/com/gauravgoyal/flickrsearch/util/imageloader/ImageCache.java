package com.gauravgoyal.flickrsearch.util.imageloader;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.*;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class handles disk and memory caching of bitmaps
 */
public class ImageCache {
    private static final String TAG = "ImageCache";

    // Default memory cache size in kilobytes
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    // Default disk cache size in bytes
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private DiskLruCache mDiskLruCache;
    private LruCache<String, BitmapDrawable> mMemoryCache;
    private ImageCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    private ImageCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    public static ImageCache getInstance(
            FragmentManager fragmentManager, ImageCacheParams cacheParams) {
        final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);
        ImageCache imageCache = (ImageCache) mRetainFragment.getObject();
        if (imageCache == null) {
            imageCache = new ImageCache(cacheParams);
            mRetainFragment.setObject(imageCache);
        }
        return imageCache;
    }


    private void init(ImageCacheParams cacheParams) {
        mCacheParams = cacheParams;
        if (mCacheParams.memoryCacheEnabled) {
            mReusableBitmaps =
                    Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());

            mMemoryCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {
                @Override
                protected void entryRemoved(boolean evicted, String key,
                                            BitmapDrawable oldValue, BitmapDrawable newValue) {
                    if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
                        ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
                    } else {
                        // The removed entry is a standard BitmapDrawable
                        mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
                    }
                }

                /**
                 * Measure item size in kilobytes rather than units which is more practical
                 * for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, BitmapDrawable value) {
                    final int bitmapSize = getBitmapSize(value) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }
        if (cacheParams.initDiskCacheOnCreate) {
            // Set up disk cache
            initDiskCache();
        }
    }


    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(
                                    diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                            Log.e(TAG, "initDiskCache - " + e);
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     * @param data Unique identifier for the bitmap to store
     * @param value The bitmap drawable to store
     */
    public void addBitmapToCache(String data, BitmapDrawable value) {
        if (data == null || value == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null) {
            if (RecyclingBitmapDrawable.class.isInstance(value)) {
                ((RecyclingBitmapDrawable) value).setIsCached(true);
            }
            mMemoryCache.put(data, value);
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            value.getBitmap().compress(
                                    mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }


    public BitmapDrawable getBitmapFromMemCache(String data) {
        BitmapDrawable memValue = null;
        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
        }
        return memValue;
    }


    public Bitmap getBitmapFromDiskCache(String data) {
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();
                            bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(
                                    fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
    }


    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }

        return bitmap;
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                } catch (IOException e) {
                    Log.e(TAG, "clearCache - " + e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;


        public ImageCacheParams(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.01 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     */
    private static int getBytesPerPixel(Config config) {
        if (config == Config.ARGB_8888) {
            return 4;
        } else if (config == Config.RGB_565) {
            return 2;
        } else if (config == Config.ARGB_4444) {
            return 2;
        } else if (config == Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();

        return bitmap.getAllocationByteCount();
    }

    public static boolean isExternalStorageRemovable() {
        return Environment.isExternalStorageRemovable();
    }


    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }


    public static long getUsableSpace(File path) {
        return path.getUsableSpace();
    }


    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }
        return mRetainFragment;
    }


    public static class RetainFragment extends Fragment {
        private Object mObject;

        public RetainFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
        public void setObject(Object object) {
            mObject = object;
        }
        public Object getObject() {
            return mObject;
        }
    }

}
