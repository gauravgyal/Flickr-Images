package com.gauravgoyal.flickrsearch.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gauravgoyal.flickrsearch.model.PhotoServiceResponse
import com.gauravgoyal.flickrsearch.repo.PhotosRepo
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ImageGridViewModel : ViewModel(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun getPhotoList(key: String, page: Int): MutableLiveData<PhotoServiceResponse> {
        val photoServiceResponse: MutableLiveData<PhotoServiceResponse> = MutableLiveData()
        launch {
            if (key.isNotEmpty()) {
                photoServiceResponse.postValue(PhotosRepo.getPhotos(key, page))
            }
        }
        return photoServiceResponse
    }


}


