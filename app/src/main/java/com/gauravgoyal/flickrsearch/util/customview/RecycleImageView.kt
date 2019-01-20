package com.gauravgoyal.flickrsearch.util.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.gauravgoyal.flickrsearch.util.imageloader.RecyclingBitmapDrawable


/**
 * Image view class , it notifies the drawables.
 */
class RecycleImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}


    override fun onDetachedFromWindow() {
        setImageDrawable(null)
        super.onDetachedFromWindow()
    }


    override fun setImageDrawable(drawable: Drawable?) {
        val previousDrawable = getDrawable()

        super.setImageDrawable(drawable)

        notifyDrawable(drawable, true)
        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false)
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     *
     * @param drawable
     * @param isDisplayed
     */
    private fun notifyDrawable(drawable: Drawable?, isDisplayed: Boolean) {
        drawable?.let {
            if (drawable is RecyclingBitmapDrawable) {
                drawable.setIsDisplayed(isDisplayed)
            } else if (drawable is LayerDrawable) {
                val layerDrawable = drawable as LayerDrawable?
                var i = 0
                val z = layerDrawable!!.numberOfLayers
                while (i < z) {
                    notifyDrawable(layerDrawable.getDrawable(i), isDisplayed)
                    i++
                }
            }
        }

    }

}
