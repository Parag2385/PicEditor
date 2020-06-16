package com.appexecutors.piceditor

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.appexecutors.piceditor.databinding.FragmentPicEditorBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.appexecutors.piceditor.editorengine.models.MediaPreview
import com.appexecutors.piceditor.editorengine.preview.MediaPreviewPagerAdapter
import com.appexecutors.piceditor.editorengine.preview.MediaThumbnailAdapter
import com.appexecutors.piceditor.editorengine.utils.AppConstants

/**
 * A simple [Fragment] subclass.
 */
class PicEditorFragment : Fragment(), MediaThumbnailAdapter.ThumbnailInterface  {

    private lateinit var mBinding: FragmentPicEditorBinding
    private lateinit var mEditOptions: EditOptions

    private lateinit var mViewModel: PicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_pic_editor, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = requireActivity().let {
            ViewModelProvider(requireActivity()).get(PicViewModel::class.java)
        }

        if (requireActivity().intent != null)
            mEditOptions = requireActivity().intent.getSerializableExtra(AppConstants.EDITOR_OPTIONS) as EditOptions

        mBinding.fragment = this
        mBinding.options = mEditOptions

        loadImages()
    }

    private fun loadImages(){

        if (mViewModel.mMediaPreviewList == null){
            val mMediaPreviewList = ArrayList<MediaPreview>()
            for (mediaUri in mEditOptions.mSelectedImageList) mMediaPreviewList.add(MediaPreview(mediaUri))

            mViewModel.mMediaPreviewList = mMediaPreviewList
        }

        val mAdapter = MediaPreviewPagerAdapter(requireActivity(), mViewModel.mMediaPreviewList!!)
        mBinding.viewPager.adapter = mAdapter

        val mThumbnailAdapter = MediaThumbnailAdapter(requireActivity(), mViewModel.mMediaPreviewList!!, this)
        mBinding.recyclerViewMedia.adapter = mThumbnailAdapter

        mBinding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mViewModel.mCurrentMediaPosition = position
                mBinding.editTextCaption.setText(mViewModel.mMediaPreviewList!![position].mCaption)
                mThumbnailAdapter.itemClick(position, 2)
            }
        })

        mBinding.editTextCaption.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val position = mBinding.viewPager.currentItem
                mViewModel.mMediaPreviewList!![position].mCaption = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/*Not Required*/}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {/*Not Required*/}
        })
    }

    fun cropImage(){
        findNavController().navigate(R.id.action_picEditorFragment_to_cropRotateFragment)
    }

    fun onBackPress() = activity?.finish()

    override fun onThumbnailSelection(position: Int, mFrom: Int) {
        if (mFrom == 1) mBinding.viewPager.currentItem = position
    }

}
