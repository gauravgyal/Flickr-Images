package com.gauravgoyal.flickrsearch.util.utility

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView


abstract class PaginationScrollListener(layoutManager: GridLayoutManager) : RecyclerView.OnScrollListener() {
    private val mLayoutManager: GridLayoutManager = layoutManager
    private val startingPageIndex = 0
    private var visibleThreshold = 5
    private var currentPage = 0
    private var previousTotalItemCount = 0
    private var loading = true
    private var hasMore: Boolean = false


    init {
        visibleThreshold *= layoutManager.spanCount
    }


    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        if (mLayoutManager.canScrollVertically() && dy >= 0) {
            handlePagination(view)
        }
    }

    private fun handlePagination(view: RecyclerView) {
        var lastVisibleItemPosition = 0
        val totalItemCount = mLayoutManager.itemCount
        lastVisibleItemPosition =
                mLayoutManager.findLastVisibleItemPosition()


        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex
            this.previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                this.loading = true
            }
        }

        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (hasMore && !loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            onLoadMore(currentPage, totalItemCount, view)
            loading = true
        }
    }

    // Call this method whenever performing new searches
    fun resetState() {
        this.currentPage = this.startingPageIndex
        this.previousTotalItemCount = 0
        this.loading = true
    }

    // use this only when network call fails.
    fun setLoading(loading: Boolean) {
        this.loading = loading
        if (!loading) currentPage--
    }

    fun setHasMore(hasMore: Boolean) {
        this.hasMore = hasMore
    }

    // Defines the process for actually loading more data based on page
    abstract fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView)

}