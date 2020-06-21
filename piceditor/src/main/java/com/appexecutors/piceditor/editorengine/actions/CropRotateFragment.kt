package com.appexecutors.piceditor.editorengine.actions

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.appexecutors.piceditor.R
import com.appexecutors.piceditor.databinding.FragmentCropRotateBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 */
class CropRotateFragment : Fragment(), CropAspectRatioAdapter.AspectRationInterface {

    private lateinit var mBinding: FragmentCropRotateBinding
    private lateinit var mViewModel: PicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_crop_rotate, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = requireActivity().let {
            ViewModelProvider(requireActivity()).get(PicViewModel::class.java)
        }

        mBinding.fragment = this

        loadImage()
    }

    private fun loadImage(){
        Log.e("CropRotateFragment", "loadImage: ${mViewModel.mCurrentMediaPosition}")
        val mProcessedBitmap = mViewModel.mMediaPreviewList!![mViewModel.mCurrentMediaPosition].mProcessedBitmap
        if (mProcessedBitmap != null){
            mBinding.cropImageView.setImageBitmap(mProcessedBitmap)
        }else {
            val mImageUri = mViewModel.mMediaPreviewList!![mViewModel.mCurrentMediaPosition].mMediaUri

            Glide.with(this)
                .setDefaultRequestOptions(RequestOptions())
                .asBitmap()
                .load(mImageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        mBinding.cropImageView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {/*Not Required*/}
                })

        }
        setCropRatioRecyclerView()
    }

    private fun setCropRatioRecyclerView(){
        val mCropRatioList = ArrayList<CropAspectRatio>()
        val ratio = CropAspectRatio(0, 0, "Free", R.drawable.ic_free_crop)
        ratio.isSelected = true
        mCropRatioList.add(ratio)
        mCropRatioList.add(CropAspectRatio(1, 1, "Square", R.drawable.ratio_1is1))
        mCropRatioList.add(CropAspectRatio(3, 4, "3:4", R.drawable.ratio_3is4))
        mCropRatioList.add(CropAspectRatio(3, 2, "3:2", R.drawable.ratio_3is2))

        val mCropRatioAdapter = CropAspectRatioAdapter(mCropRatioList, this)
        mBinding.recyclerViewRatio.adapter = mCropRatioAdapter
    }

    fun rotateImage() = mBinding.cropImageView.rotateImage(90)

    fun doneCropping(){
        CoroutineScope(Dispatchers.Main).launch{
            val mCroppedBitmap = withContext(Dispatchers.IO){
                mBinding.cropImageView.croppedImage
            }
            if (mViewModel.mMediaPreviewList != null){
                mViewModel.mMediaPreviewList!![mViewModel.mCurrentMediaPosition].mProcessedBitmap = mCroppedBitmap
            }
            findNavController().navigate(CropRotateFragmentDirections.actionCropRotateFragmentToPicEditorFragment())
        }
    }

    fun cancel() = findNavController().navigate(CropRotateFragmentDirections.actionCropRotateFragmentToPicEditorFragment())

    override fun onAspectRatioSelected(mRatio: CropAspectRatio) {
        if (mRatio.xCoordinate > 0)
            mBinding.cropImageView.setAspectRatio(mRatio.xCoordinate, mRatio.yCoordinate)
        else
            mBinding.cropImageView.clearAspectRatio()
    }
}
