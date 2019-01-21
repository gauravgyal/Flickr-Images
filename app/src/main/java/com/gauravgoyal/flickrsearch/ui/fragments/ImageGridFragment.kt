package com.gauravgoyal.flickrsearch.ui.fragments

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.ui.adapters.ImageAdapter
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.util.imageloader.ImageFetcher
import com.gauravgoyal.flickrsearch.util.utility.PaginationScrollListener
import com.gauravgoyal.flickrsearch.util.utility.setCacheParams
import kotlinx.android.synthetic.main.image_grid_fragment.*
import java.util.*

/**
 * The main fragment that powers the ImageGridActivity screen
 */
class ImageGridFragment : BaseFragment() {


    private val mAdapter: ImageAdapter by lazy { ImageAdapter(mImageFetcher, gridView.getColWidth()) }
    private val mImageFetcher: ImageFetcher by lazy {
        ImageFetcher(
            activity,
            resources.getDimensionPixelSize(R.dimen.image_thumbnail_size)
        )
    }

    private val paginationScrollListener: PaginationScrollListener by lazy {
        object : PaginationScrollListener((gridView.layoutManager as GridLayoutManager?)!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                paginationScrollListener.setLoading(true)
                loadResult(page + 1)

            }
        }
    }

    override fun showLoader(active: Boolean) {
        progressBar.visibility = if (active) View.VISIBLE else View.GONE
    }

    override fun showErrorBox() {
        Toast.makeText(requireContext(), R.string.error_text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        activity?.let { mImageFetcher.setCacheParams(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.image_grid_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        gridView.adapter = mAdapter
        gridView.addOnScrollListener(paginationScrollListener)
    }

    override fun onResume() {
        super.onResume()
        mImageFetcher.setExitTasksEarly(false)
    }

    override fun onPause() {
        super.onPause()
        mImageFetcher.setPauseWork(false)
        mImageFetcher.setExitTasksEarly(true)
        mImageFetcher.flushCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        mImageFetcher.closeCache()
    }

    override fun loadPhotos(photoList: ArrayList<PhotoModel>) {
        mAdapter.addPhotos(photoList)
    }

    override fun hasMore(hasMore: Boolean) {
        paginationScrollListener.setHasMore(hasMore)
    }

    override fun resetView() {
        paginationScrollListener.resetState()
        mAdapter.clearPhotos()
    }
}
