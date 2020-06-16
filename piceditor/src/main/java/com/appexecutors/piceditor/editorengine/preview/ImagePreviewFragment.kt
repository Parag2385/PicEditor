package com.appexecutors.piceditor.editorengine.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.appexecutors.piceditor.R
import com.appexecutors.piceditor.databinding.FragmentImagePreviewBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.appexecutors.piceditor.editorengine.utils.AppConstants.MEDIA_POSITION
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * A simple [Fragment] subclass.
 */
class ImagePreviewFragment : Fragment() {

    private lateinit var mBinding: FragmentImagePreviewBinding
    private lateinit var mViewModel: PicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_image_preview, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = requireActivity().let {
            ViewModelProvider(requireActivity()).get(PicViewModel::class.java)
        }

        val mCurrentPosition = arguments?.getInt(MEDIA_POSITION)

        if (mViewModel.mMediaPreviewList != null){
            val mProcessedBitmap = mViewModel.mMediaPreviewList!![mCurrentPosition!!].mProcessedBitmap
            if (mProcessedBitmap != null){
                mBinding.photoView.setImageBitmap(mProcessedBitmap)
            }else{
                loadImage(mViewModel.mMediaPreviewList!![mCurrentPosition].mMediaUri)
            }
        }
    }

    private fun loadImage(mImageUri: String?){
        Glide.with(this)
            .setDefaultRequestOptions(RequestOptions())
            .load(mImageUri)
            .into(mBinding.photoView)

        //todo: provision to add watermark
    }
}
