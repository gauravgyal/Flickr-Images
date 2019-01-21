package com.gauravgoyal.flickrsearch.util.utility

import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.util.imageloader.ImageCache
import com.gauravgoyal.flickrsearch.util.imageloader.ImageFetcher


fun ImageFetcher.setCacheParams(activity: FragmentActivity) {
    val cacheParams = ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR)
    cacheParams.setMemCacheSizePercent(0.25f) // Set memory cache to 25% of app memory
    setLoadingImage(R.drawable.empty_photo)
    addImageCache(activity.supportFragmentManager, cacheParams)
}



fun PhotoModel.getImageUrl():String {
    val sb = StringBuffer()
    sb.append("http://farm")
    sb.append(farm)
    sb.append(".static.flickr.com/")
    sb.append(server)
    sb.append("/")
    sb.append(id)
    sb.append("_")
    sb.append(secret)
    sb.append(".jpg")

    return sb.toString()
}