package com.appexecutors.piceditor

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ACTION_STARTED
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ACTION_STOPPED
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ADD_BRUSH_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ADD_TEXT_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.DISABLE_BRUSH_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDIT_TEXT_ACTION_DONE
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDIT_TEXT_ACTION_START
import com.appexecutors.piceditor.editorengine.utils.AppConstants.INTENT_FROM_PIC_EDITOR
import com.appexecutors.piceditor.editorengine.utils.AppConstants.INTENT_FROM_PIC_EDITOR_FRAGMENT
import com.appexecutors.piceditor.editorengine.utils.GlobalEventListener
import com.appexecutors.piceditor.editorengine.utils.ToolType
import com.appexecutors.piceditor.editorengine.utils.Utils
import com.appexecutors.piceditor.editorengine.utils.keyboard.KeyboardHeightObserver
import com.appexecutors.piceditor.editorengine.utils.keyboard.KeyboardHeightProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
 * A simple [Fragment] subclass.
 */
class PicEditorFragment : Fragment(), MediaThumbnailAdapter.ThumbnailInterface,
    KeyboardHeightObserver {

    private lateinit var mBinding: FragmentPicEditorBinding
    private lateinit var mEditOptions: EditOptions
    private lateinit var mInputMethodManager: InputMethodManager
    private lateinit var mViewModel: PicViewModel
    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

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

        keyboardHeightProvider = KeyboardHeightProvider(requireActivity())

        mBinding.root.post {
            keyboardHeightProvider.start()
        }

        mViewModel = requireActivity().let {
            ViewModelProvider(requireActivity()).get(PicViewModel::class.java)
        }

        if (requireActivity().intent != null)
            mEditOptions = requireActivity().intent.getSerializableExtra(AppConstants.EDITOR_OPTIONS) as EditOptions

        mBinding.fragment = this
        mBinding.options = mEditOptions

        loadImages()

        mInputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private var mMediaPreviewAdapter : MediaPreviewPagerAdapter? = null
    private var mThumbnailAdapter : MediaThumbnailAdapter? = null

    private var mPicketColor = Color.parseColor("#03A9F4") //initialised to blue

    private fun loadImages(){

        if (mViewModel.mMediaPreviewList == null){
            val mMediaPreviewList = ArrayList<MediaPreview>()
            for (mediaUri in mEditOptions.mSelectedImageList) mMediaPreviewList.add(MediaPreview(mediaUri))

            mViewModel.mMediaPreviewList = mMediaPreviewList
        }

        mMediaPreviewAdapter = MediaPreviewPagerAdapter(requireActivity(), mViewModel.mMediaPreviewList!!)
        mBinding.viewPager.adapter = mMediaPreviewAdapter

        mThumbnailAdapter = MediaThumbnailAdapter(requireActivity(), mViewModel.mMediaPreviewList!!, this)
        mBinding.recyclerViewMedia.adapter = mThumbnailAdapter
        mThumbnailAdapter?.mSelectedPosition = mViewModel.mCurrentMediaPosition

        mBinding.viewPager.setCurrentItem(mViewModel.mCurrentMediaPosition, false)

        mBinding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mViewModel.mCurrentMediaPosition = position
                if (mViewModel.mMediaPreviewList?.size!! > 0) {
                    mBinding.editTextCaption.setText(mViewModel.mMediaPreviewList!![position].mCaption)
                    mThumbnailAdapter?.itemClick(position, 2)
                }
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

        mBinding.colorPicker.resetToDefault()

        mBinding.colorPicker.setOnColorChangeListener {
            if (it != 0) mPicketColor = it

            if (mPickedTool == ToolType.TEXT || mPickedTool == ToolType.TEXT_EDIT){
                mBinding.editText.setTextColor(mPicketColor)
                mBinding.imageViewText.background = Utils.tintDrawable(requireContext(), R.drawable.shape_circle, mPicketColor)
            }else if(mPickedTool == ToolType.BRUSH) {
                startBrush()
            }
        }

        captionClickListener()
    }

    fun deleteImage(){
        if (mViewModel.mMediaPreviewList?.size!! > 1) {
            mViewModel.mMediaPreviewList?.removeAt(mViewModel.mCurrentMediaPosition)
            mThumbnailAdapter?.removeThumbnail(mViewModel.mCurrentMediaPosition)
            mMediaPreviewAdapter?.notifyItemRemoved(mViewModel.mCurrentMediaPosition)
            if (mViewModel.mCurrentMediaPosition != mViewModel.mMediaPreviewList?.size!!){
                mBinding.editTextCaption.setText(mViewModel.mMediaPreviewList!![mViewModel.mCurrentMediaPosition].mCaption)
                mThumbnailAdapter?.itemClick(mViewModel.mCurrentMediaPosition, 2)
            }
        }else onBackPress()
    }

    fun cropImage() = findNavController().navigate(R.id.action_picEditorFragment_to_cropRotateFragment)

    var mPickedTool = ToolType.NONE

    fun addTextOver(){
        setTool(ToolType.TEXT)
    }

    fun textDone(){

        val action = if (mPickedTool == ToolType.TEXT_EDIT) EDIT_TEXT_ACTION_DONE else ADD_TEXT_ACTION
        mPickedTool = ToolType.NONE
        mBinding.colorPicker.visibility = INVISIBLE
        if (mBinding.editText.text.toString().isNotEmpty()) {
            val event = GlobalEventListener(action)
            event.mText = mBinding.editText.text.toString()
            event.mTextColor = mBinding.editText.currentTextColor
            EventBus.getDefault().post(event)
        }
        clearText(INTENT_FROM_PIC_EDITOR_FRAGMENT)
        animateBackIcon(true)
    }

    fun clearText(mIntentFrom: Int){
        mBinding.constraintLayoutText.visibility = GONE
        mBinding.editText.clearFocus()
        mBinding.editText.setText("")
        mInputMethodManager.hideSoftInputFromWindow(mBinding.root.windowToken, 0)
        mBinding.imageViewText.setBackgroundResource(android.R.color.transparent)
        if (mIntentFrom == INTENT_FROM_PIC_EDITOR) mBinding.colorPicker.visibility = INVISIBLE
    }


    fun startBrush(){
        setTool(ToolType.BRUSH)
        val event = GlobalEventListener(ADD_BRUSH_ACTION)
        event.mTextColor = mPicketColor
        EventBus.getDefault().post(event)
    }

    fun brushDone(){
        mPickedTool = ToolType.NONE
        mBinding.colorPicker.visibility = INVISIBLE
        mBinding.viewPager.isUserInputEnabled = true
        clearBrush()
    }

    private fun clearBrush(){
        mBinding.imageViewDraw.setBackgroundResource(android.R.color.transparent)

        val event = GlobalEventListener(DISABLE_BRUSH_ACTION)
        EventBus.getDefault().post(event)
    }

    private fun setTool(mToolType: ToolType){
        mPickedTool = mToolType
        if (mPickedTool == ToolType.BRUSH){
            mBinding.viewPager.isUserInputEnabled = false
            mBinding.colorPicker.visibility = VISIBLE
            mBinding.imageViewDraw.background = Utils.tintDrawable(requireContext(), R.drawable.shape_circle, mPicketColor)
            animateBackIcon(false)
            clearText(INTENT_FROM_PIC_EDITOR_FRAGMENT)
        }else if (mPickedTool == ToolType.TEXT || mPickedTool == ToolType.TEXT_EDIT){
            mBinding.constraintLayoutText.visibility = VISIBLE
            mBinding.colorPicker.visibility = VISIBLE
            mBinding.editText.requestFocus()
            mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            if (mPickedTool == ToolType.TEXT_EDIT) {
                mBinding.editText.setText(mClickedText)
                mBinding.editText.setTextColor(mClickedColor)
            }
            mBinding.imageViewText.background = resources.getDrawable(R.drawable.shape_circle, null)
            animateBackIcon(false)
            clearBrush()
        }
    }

    fun onBackPress() = activity?.onBackPressed()

    override fun onThumbnailSelection(position: Int, mFrom: Int) {
        if (mFrom == 1) mBinding.viewPager.setCurrentItem(position, false)
    }

    fun animateBackIcon(showBack: Boolean){
        if(showBack){
            mBinding.imageViewBack.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_clear_to_back))
            mBinding.imageViewBack.post {
                val frameAnimation =
                    mBinding.imageViewBack.drawable as AnimatedVectorDrawable
                frameAnimation.start()
            }
        }else{
            mBinding.imageViewBack.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_back_to_clear))
            mBinding.imageViewBack.post {
                val frameAnimation =
                    mBinding.imageViewBack.drawable as AnimatedVectorDrawable
                frameAnimation.start()
            }
        }
    }

    private fun captionClickListener(){
        mBinding.editTextCaption.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) mPickedTool = ToolType.TEXT_CAPTION
        }
        mBinding.editTextCaption.setOnClickListener {
            mPickedTool = ToolType.TEXT_CAPTION
        }
    }

    private var mClickedText = ""
    private var mClickedColor = -1

    @Subscribe
    fun onGlobalEventListener(mEvent: GlobalEventListener){
        when (mEvent.mActionID) {
            EDIT_TEXT_ACTION_START ->{
                mPickedTool = ToolType.TEXT_EDIT
                mClickedText = mEvent.mText
                mClickedColor = mEvent.mTextColor
                setTool(ToolType.TEXT_EDIT)
            }
            ACTION_STARTED -> mBinding.viewPager.isUserInputEnabled = false
            ACTION_STOPPED -> mBinding.viewPager.isUserInputEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        Log.e("PicEditorFragment", "onKeyboardHeightChanged: in pixels: $height")

        if (height > 0 && mPickedTool == ToolType.TEXT_CAPTION){

            val finalHeight = height - mBinding.recyclerViewMedia.height

            val params = mBinding.constraintLayoutCaption.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, finalHeight)
            mBinding.constraintLayoutCaption.layoutParams = params
        }else{
            val params = mBinding.constraintLayoutCaption.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, 0)
            mBinding.constraintLayoutCaption.layoutParams = params
        }
    }

    override fun onPause() {
        super.onPause()
        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider.setKeyboardHeightObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }
}
