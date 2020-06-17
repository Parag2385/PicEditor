package com.appexecutors.piceditor.editorengine.actions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appexecutors.piceditor.R
import com.appexecutors.piceditor.databinding.RecyclerItemCropRatioBinding

class CropAspectRatioAdapter(private val mRatioList: ArrayList<CropAspectRatio>,
                             private val mInterface: AspectRationInterface):
RecyclerView.Adapter<CropAspectRatioAdapter.CropAspectRatioViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropAspectRatioViewHolder {
        val mBinding = RecyclerItemCropRatioBinding.inflate(LayoutInflater.from(parent.context), null, false)
        return CropAspectRatioViewHolder(mBinding)
    }

    override fun getItemCount(): Int = mRatioList.size

    override fun onBindViewHolder(holder: CropAspectRatioViewHolder, position: Int)
            = holder.bind(mRatioList[holder.adapterPosition], holder.adapterPosition)

    inner class CropAspectRatioViewHolder(private val mBinding: RecyclerItemCropRatioBinding) : RecyclerView.ViewHolder(mBinding.root){

        fun bind(mRatio: CropAspectRatio, position: Int){
            mBinding.ratio = mRatio

            itemView.setOnClickListener {
                onClickAspectRatio(mRatio, position)
            }

            if (mRatio.isSelected){
                mBinding.linearLayoutIcon.setBackgroundResource(R.drawable.background_alpha_blue_dark_border)
            }else{
                mBinding.linearLayoutIcon.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    private var mSelectedPosition = 0

    fun onClickAspectRatio(mRatio: CropAspectRatio, position: Int){
        if (mSelectedPosition == RecyclerView.NO_POSITION) {
            mSelectedPosition = position
            mRatioList[mSelectedPosition].isSelected = true
            notifyItemChanged(mSelectedPosition)
        } else {
            mRatioList[mSelectedPosition].isSelected = false
            notifyItemChanged(mSelectedPosition)
            mSelectedPosition = position
            mRatioList[mSelectedPosition].isSelected = true
            notifyItemChanged(mSelectedPosition)
        }
        mInterface.onAspectRatioSelected(mRatio)
    }

    interface AspectRationInterface{
        fun onAspectRatioSelected(mRatio: CropAspectRatio)
    }
}