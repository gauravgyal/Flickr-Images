package com.gauravgoyal.flickrsearch.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gauravgoyal.flickrsearch.R
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.util.customview.RecycleImageView
import com.gauravgoyal.flickrsearch.util.lifecycle.PhotoLoader
import com.gauravgoyal.flickrsearch.util.utility.getImageUrl
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import java.util.*

class ImageAdapter(private val mPhotoLoader: PhotoLoader, width: Int) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ImageAdapter.ItemViewHolder>() {

    private val photosList: MutableList<PhotoModel> by lazy { ArrayList<PhotoModel>() }
    private var context: Context? = null
    private val mImageViewLayoutParams: LayoutParams = LayoutParams(
        LayoutParams.MATCH_PARENT, width
    )

    inner class ItemViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
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
        mPhotoLoader.loadPhoto(photosList[position].getImageUrl(), holder.img)
    }

    override fun getItemCount(): Int {
        return photosList.size
    }
}

