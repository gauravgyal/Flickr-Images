package com.gauravgoyal.flickrsearch.contract


import com.gauravgoyal.flickrsearch.model.PhotoServiceResponse
import com.gauravgoyal.flickrsearch.repo.PhotosRepo


class ListPresenter(private val view: ListContract.View) : ListContract.Presenter,
    PhotosRepo.OnLoadListener {

    override fun loadPhotos(searchText: String, page: Int) {
        PhotosRepo(searchText, page, this).execute()
    }


    override fun create() {

    }

    override fun start() {

    }

    override fun stop() {

    }

    override fun destroy() {

    }

    override fun onError() {
        view.setLoadingIndicator(false)
        view.showErrorBox()
    }

    override fun startSearch(searchText: String, page: Int) {
        view.setLoadingIndicator(true)
        loadPhotos(searchText, page)
    }

    override fun onLoadComplete(response: PhotoServiceResponse) {
        view.setLoadingIndicator(false)
        view.showList(response)
    }


}
