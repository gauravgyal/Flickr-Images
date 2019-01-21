package com.gauravgoyal.flickrsearch.ui.activity


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.ui.fragments.BaseFragment
import com.gauravgoyal.flickrsearch.ui.fragments.ImageGridFragment
import com.gauravgoyal.flickrsearch.util.utility.DEFAULT_SEARCH_KEY

/**
 * Simple Activity to hold the main [ImageGridFragment] and not much else.
 */
class ImageGridActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentByTag(TAG) == null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.add(android.R.id.content, ImageGridFragment(), TAG)
            ft.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val searchViewMenuItem = menu.findItem(R.id.search)
        val searchView = searchViewMenuItem.actionView as android.support.v7.widget.SearchView

        searchView.setQuery(DEFAULT_SEARCH_KEY,true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //Do your search
                doSearch(key = query)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        return true
    }

    fun doSearch(key: String) {
        val baseFragment = supportFragmentManager.findFragmentByTag(TAG) as? BaseFragment
        baseFragment?.loadResult(key)
    }

    companion object {
        private val TAG = "ImageGridActivity"
    }


}
