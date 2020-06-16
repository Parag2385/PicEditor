package com.appexecutors.piceditor.editorengine.actions

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
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
class CropRotateFragment : Fragment() {

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

        loadImage(mViewModel.mMediaPreviewList!![mViewModel.mCurrentMediaPosition].mMediaUri)
    }

    private fun loadImage(mImageUri: String?){
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

    fun cancel() = findNavController().navigateUp()
}
