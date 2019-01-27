package com.gauravgoyal.flickrsearch.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.ui.adapters.ImageAdapter
import com.gauravgoyal.flickrsearch.util.lifecycle.PhotoLoader
import com.gauravgoyal.flickrsearch.util.utility.DEFAULT_SEARCH_KEY
import com.gauravgoyal.flickrsearch.util.utility.PaginationScrollListener
import com.gauravgoyal.flickrsearch.util.utility.SEARCH_KEY
import com.gauravgoyal.flickrsearch.viewmodel.ImageGridViewModel
import kotlinx.android.synthetic.main.image_grid_fragment.*
import java.util.*


/**
 * The main fragment that powers the ImageGridActivity screen
 */
class ImageGridFragment : Fragment() {
    private val mAdapter: ImageAdapter by lazy { ImageAdapter(mPhotoLoader, gridView.getColWidth()) }
    private val mPhotoLoader: PhotoLoader by lazy {
        PhotoLoader(
            requireActivity(),
            gridView.getColWidth()
        )
    }

    private lateinit var searchKey: String
    private val vm: ImageGridViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(
            ImageGridViewModel::class.java
        )
    }

    private val paginationScrollListener: PaginationScrollListener by lazy {
        object : PaginationScrollListener((gridView.layoutManager as GridLayoutManager?)!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                paginationScrollListener.setLoading(true)
                loadResults(page + 1)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.image_grid_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            searchKey = savedInstanceState.getString(SEARCH_KEY)
        }
        gridView.adapter = mAdapter
        gridView.addOnScrollListener(paginationScrollListener)
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.let {
            val searchViewMenuItem = menu.findItem(com.gauravgoyal.flickrsearch.R.id.search)
            val searchView = searchViewMenuItem.actionView as androidx.appcompat.widget.SearchView

            searchView.setQuery(DEFAULT_SEARCH_KEY, true)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    //Do your search
                    loadResults(key = query)
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_KEY, searchKey)
    }

    private fun resetView() {
        paginationScrollListener.resetState()
        mAdapter.clearPhotos()
    }

    private fun showLoader(active: Boolean) {
        progressBar.visibility = if (active) View.VISIBLE else View.GONE
    }

    private fun showErrorBox() {
        Toast.makeText(requireContext(), R.string.error_text, Toast.LENGTH_SHORT).show()
    }

    private fun fetchData(key: String, page: Int) {
        vm.getPhotoList(key, page).observe(this, Observer { response ->
            response?.let {
                showLoader(false)
                paginationScrollListener.setHasMore(response.currentPage != response.totalPages)
                mAdapter.addPhotos(it.data)
            } ?: kotlin.run {
                showErrorBox()
                fetchData(key, page)
            }
        })
    }

    //1st time call
    fun loadResults(key: String) {
        searchKey = key
        resetView()
        showLoader(true)
        fetchData(key, 1)
    }

    fun loadResults(page: Int) {
        showLoader(true)
        fetchData(searchKey, page)
    }

    @VisibleForTesting
    fun loadPhotos(photoList: ArrayList<PhotoModel>) {
        mAdapter.addPhotos(photoList)
    }
}
