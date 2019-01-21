package com.gauravgoyal.flickrsearch.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.util.customview.RecycleImageView
import com.gauravgoyal.flickrsearch.util.imageloader.ImageFetcher
import com.gauravgoyal.flickrsearch.util.utility.getImageUrl
import java.util.*

class ImageAdapter(private val mImageFetcher: ImageFetcher, val width: Int) :
    RecyclerView.Adapter<ImageAdapter.ItemViewHolder>() {

    private val photosList: MutableList<PhotoModel> by lazy { ArrayList<PhotoModel>() }
    private var context: Context? = null
    private val mImageViewLayoutParams: RecyclerView.LayoutParams = RecyclerView.LayoutParams(
        RecyclerView.LayoutParams.MATCH_PARENT, width
    )

    init {
        mImageFetcher.setImageSize(width)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var img: RecycleImageView = view.findViewById(R.id.img)

    }


    fun addPhotos(photosList: List<PhotoModel>) {
        val beforeSize = this.photosList.size
        this.photosList.addAll(photosList)
        notifyItemRangeInserted(beforeSize, photosList.size)
    }

    fun clearPhotos() {
        val size = photosList.size
        photosList.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        context = parent.context

        itemView.findViewById<View>(R.id.img).layoutParams = mImageViewLayoutParams

        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        mImageFetcher.loadImage(photosList[position].getImageUrl(), holder.img)
    }

    override fun getItemCount(): Int {
        return photosList.size
    }
}

