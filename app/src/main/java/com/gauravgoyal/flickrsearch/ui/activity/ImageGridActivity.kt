package com.gauravgoyal.flickrsearch.ui.activity



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.ui.fragments.ImageGridFragment

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


    companion object {
        private const val TAG = "ImageGridActivity"
    }


}
