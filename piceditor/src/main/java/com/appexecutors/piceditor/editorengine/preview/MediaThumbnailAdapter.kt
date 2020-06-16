package com.appexecutors.piceditor.editorengine.preview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appexecutors.piceditor.R
import com.appexecutors.piceditor.databinding.RecyclerItemMediaThumbnailBinding
import com.appexecutors.piceditor.editorengine.models.MediaPreview
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MediaThumbnailAdapter(val mContext: Context, private val mMediaList: ArrayList<MediaPreview>, private val mInterface: ThumbnailInterface)
    : RecyclerView.Adapter<MediaThumbnailAdapter.ThumbnailViewHolder>(){

    private var mSelectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val mBinding =
            RecyclerItemMediaThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThumbnailViewHolder(mBinding)
    }

    override fun getItemCount(): Int = mMediaList.size

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(mMediaList[holder.adapterPosition], holder.adapterPosition)
    }

    inner class ThumbnailViewHolder(private val mBinding: RecyclerItemMediaThumbnailBinding)
        : RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mediaPreview: MediaPreview, position: Int) {
            mBinding.imageViewThumbnail.setOnClickListener { itemClick(position, 1) }

            Glide.with(mContext)
                .setDefaultRequestOptions(RequestOptions())
                .load(mediaPreview.mMediaUri)
                .into(mBinding.imageViewThumbnail)

            if (mediaPreview.isSelected){
                mBinding.imageViewThumbnail.setBackgroundResource(R.drawable.background_transparent_light_border)
            }else{
                mBinding.imageViewThumbnail.setBackgroundResource(android.R.color.transparent)
            }

        }
    }

    fun itemClick(position: Int, mFrom: Int) {

        if (mSelectedPosition == RecyclerView.NO_POSITION) {
            mSelectedPosition = position
            mMediaList[mSelectedPosition].isSelected = true
            notifyItemChanged(mSelectedPosition)
        } else {
            mMediaList[mSelectedPosition].isSelected = false
            notifyItemChanged(mSelectedPosition)
            mSelectedPosition = position
            mMediaList[mSelectedPosition].isSelected = true
            notifyItemChanged(mSelectedPosition)
        }

        if (mFrom == 1)
            mInterface.onThumbnailSelection(position, mFrom)
    }

    interface ThumbnailInterface{
        fun onThumbnailSelection(position: Int, mFrom: Int)
    }
}