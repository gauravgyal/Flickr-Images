package com.gauravgoyal.flickrsearch.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import com.gauravgoyal.flickrsearch.contract.ListContract
import com.gauravgoyal.flickrsearch.contract.ListPresenter
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.model.PhotoServiceResponse

abstract class BaseFragment : Fragment(), ListContract.View {

    private val listPresenter: ListPresenter by lazy { ListPresenter(this) }
    private lateinit var searchKey: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listPresenter.create()
    }

    override fun onStart() {
        super.onStart()
        listPresenter.start()
    }

    override fun onStop() {
        super.onStop()

        listPresenter.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        listPresenter.destroy()
    }


    //1st time call
    fun loadResult(key: String) {
        if(key.isNotEmpty()) {
            searchKey = key
            resetView()
            listPresenter.startSearch(key, 1)
        }
    }

    fun loadResult(page: Int) {
        listPresenter.startSearch(searchKey, page)
    }




    override fun setLoadingIndicator(active: Boolean) {
        showLoader(active)
    }

    override fun showList(response: PhotoServiceResponse?) {
        hasMore(!(response?.currentPage == response?.totalPages))
        response?.data?.let {
            loadPhotos(response.data)
        }
    }


    abstract fun loadPhotos(photoList: ArrayList<PhotoModel>)
    abstract fun showLoader(active: Boolean)
    abstract fun resetView()
    abstract fun hasMore(hasMore: Boolean)

}
