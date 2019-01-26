package com.gauravgoyal.flickrsearch.util.lifecycle

import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gauravgoyal.flickrsearch.util.imageloader.ImageFetcher
import com.gauravgoyal.flickrsearch.util.utility.setCacheParams

class PhotoLoader(
    private val activity: FragmentActivity,
    imageSize: Int
) : LifecycleObserver {

    private val mImageFetcher: ImageFetcher = ImageFetcher(
        activity,
        imageSize
    )
    private val mLifecycle: Lifecycle = activity.lifecycle

    init {
        mLifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        mImageFetcher.setCacheParams(activity = activity)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        mImageFetcher.setExitTasksEarly(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        mImageFetcher.setPauseWork(false)
        mImageFetcher.setExitTasksEarly(true)
        mImageFetcher.flushCache()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanUp() {
        mImageFetcher.closeCache()
        mLifecycle.removeObserver(this)
    }

//    fun setImageSize(size: Int) {
//        mImageFetcher.setImageSize(size)
//    }

    fun loadPhoto(url: String, view: ImageView) {
        mImageFetcher.loadImage(url, view)
    }

}