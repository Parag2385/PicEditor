package com.appexecutors.piceditor

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.appexecutors.piceditor.databinding.FragmentPicEditorBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.appexecutors.piceditor.editorengine.models.MediaFinal
import com.appexecutors.piceditor.editorengine.models.MediaPreview
import com.appexecutors.piceditor.editorengine.preview.ImagePreviewFragment
import com.appexecutors.piceditor.editorengine.preview.MediaPreviewPagerAdapter
import com.appexecutors.piceditor.editorengine.preview.MediaThumbnailAdapter
import com.appexecutors.piceditor.editorengine.utils.*
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ACTION_STARTED
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ACTION_STOPPED
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ADD_BRUSH_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ADD_TEXT_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.DISABLE_BRUSH_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDITED_MEDIA_LIST
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDIT_TEXT_ACTION_DONE
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDIT_TEXT_ACTION_START
import com.appexecutors.piceditor.editorengine.utils.AppConstants.IMAGE
import com.appexecutors.piceditor.editorengine.utils.AppConstants.INTENT_FROM_PIC_EDITOR
import com.appexecutors.piceditor.editorengine.utils.AppConstants.INTENT_FROM_PIC_EDITOR_FRAGMENT
import com.appexecutors.piceditor.editorengine.utils.AppConstants.SAVE_BITMAP_FOR_CROP_ACTION_DONE
import com.appexecutors.piceditor.editorengine.utils.AppConstants.UNDO_REDO_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.VIDEO
import com.appexecutors.piceditor.editorengine.utils.Utils.disableEnableControls
import com.appexecutors.piceditor.editorengine.utils.keyboard.KeyboardHeightObserver
import com.appexecutors.piceditor.editorengine.utils.keyboard.KeyboardHeightProvider
import com.github.veritas1.verticalslidecolorpicker.VerticalSlideColorPicker
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
 * A simple [Fragment] subclass.
 */
class PicEditorFragment : Fragment(), MediaThumbnailAdapter.ThumbnailInterface,
    KeyboardHeightObserver {

    private lateinit var mBinding: FragmentPicEditorBinding
    lateinit var mEditOptions: EditOptions
    private lateinit var mInputMethodManager: InputMethodManager
    private lateinit var mViewModel: PicViewModel
    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        mInputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (requireActivity().intent != null)
            mEditOptions = requireActivity().intent.getSerializableExtra(AppConstants.EDITOR_OPTIONS) as EditOptions

        initAll()
    }

    fun initAll(){
        mBinding.fragment = this
        mBinding.options = mEditOptions
        mViewModel.mEditOptions = mEditOptions

        loadImages()

        if (!mEditOptions.showCaption){
            val set = ConstraintSet()
            set.clone(mBinding.constraintLayoutCaption)
            set.clear(R.id.fab_select, ConstraintSet.TOP)
            set.connect(R.id.fab_select, ConstraintSet.BOTTOM, R.id.constraint_layout_caption, ConstraintSet.BOTTOM, 24)
            set.applyTo(mBinding.constraintLayoutCaption)
        }

        if (!mEditOptions.showDrawOption && !mEditOptions.showTextOption){
            mBinding.imageViewUndo.visibility = GONE
            mBinding.imageViewRedo.visibility = GONE
        }
    }

    private fun checkIfImagesAreCropped(){

        val delay = if(mViewModel.mCurrentMediaPosition == 0) 1000L else 100L

        if (mEditOptions.openWithCropOption){
            Handler(Looper.getMainLooper()).postDelayed({
                mViewModel.mMediaPreviewList?.let {
                    if (!it[mViewModel.mCurrentMediaPosition].mImageCropped){
                        if (mMediaPreviewAdapter?.getCurrentFragment(mViewModel.mCurrentMediaPosition) is ImagePreviewFragment) {
                            val mImageFragment =
                                mMediaPreviewAdapter?.getCurrentFragment(mViewModel.mCurrentMediaPosition) as ImagePreviewFragment?
                            mImageFragment?.saveSingleBitmap()
                        }
                    }
                }

            }, delay)
        }
    }

    private var mMediaPreviewAdapter : MediaPreviewPagerAdapter? = null
    private var mThumbnailAdapter : MediaThumbnailAdapter? = null

    private var mPicketColor = Color.parseColor("#03A9F4") //initialised to blue

    private fun loadImages(){

        if (mViewModel.mMediaPreviewList == null){
            val mMediaPreviewList = ArrayList<MediaPreview>()
            for (media in mEditOptions.mSelectedImageList){
                val mMediaPreview = MediaPreview(media.mMediaUri)
                mMediaPreview.mOldMediaUri = if (media.mOldMediaUri.isNotEmpty())  media.mOldMediaUri else media.mMediaUri
                mMediaPreview.mCaption = media.mCaption
                mMediaPreviewList.add(mMediaPreview)
            }

            mViewModel.mMediaPreviewList = mMediaPreviewList
        }

        mMediaPreviewAdapter = MediaPreviewPagerAdapter(requireActivity(), mViewModel.mMediaPreviewList!!)
        mBinding.viewPager.adapter = mMediaPreviewAdapter
        if(mViewModel.mMediaPreviewList?.size!! > 0) mBinding.viewPager.offscreenPageLimit = mViewModel.mMediaPreviewList?.size!!

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

                    mBinding.mode = (if (mMediaPreviewAdapter?.getCurrentFragment(position) is ImagePreviewFragment) IMAGE else VIDEO)

                    checkIfImagesAreCropped()
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

        colorPicker()

        captionClickListener()
    }

    fun addMore(){
        clearBrush()
        CoroutineScope(Dispatchers.Main).launch {
            getAllImages(false)
            mViewModel.mMediaPreviewList?.mapIndexed{ i, media ->
                mViewModel.mMediaFinalList!![i].mOldMediaUri = media.mOldMediaUri
            }
            mEditOptions.mSelectedImageList = mViewModel.mMediaFinalList!!
            mViewModel.mMediaPreviewList = null
            (requireActivity() as PicEditor).addMoreImages()
        }
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

    fun cropImage(){
        val mImageFragment = mMediaPreviewAdapter?.getCurrentFragment(mViewModel.mCurrentMediaPosition) as ImagePreviewFragment?
        mImageFragment?.saveSingleBitmap()
    }

    var mPickedTool = ToolType.NONE

    fun addTextOver(){
        if (mPickedTool == ToolType.TEXT){
            clearText(INTENT_FROM_PIC_EDITOR_FRAGMENT)
            animateBackIcon(true)
            mPickedTool = ToolType.NONE
        }else setTool(ToolType.TEXT)
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
        if (mPickedTool == ToolType.BRUSH){
            brushDone()
            animateBackIcon(true)
            mPickedTool = ToolType.NONE
        }else{
            setTool(ToolType.BRUSH)
            val event = GlobalEventListener(ADD_BRUSH_ACTION)
            event.mTextColor = mPicketColor
            EventBus.getDefault().post(event)
        }

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
            mBinding.imageViewText.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_circle)
            animateBackIcon(false)
            clearBrush()
        }
    }

    fun onBackPress() = activity?.onBackPressed()

    fun undoRedo(mFlag: Int){
        val event = GlobalEventListener(UNDO_REDO_ACTION)
        event.mUndoRedoFlag = mFlag
        EventBus.getDefault().post(event)
    }

    override fun onThumbnailSelection(position: Int, mFrom: Int) {
        if (mFrom == 1){
            if (mPickedTool != ToolType.NONE) {
                clearBrush()
                animateBackIcon(true)
                mBinding.colorPicker.visibility = INVISIBLE
            }
            mBinding.viewPager.setCurrentItem(position, false)
        }
    }

    fun animateBackIcon(showBack: Boolean){
        if(showBack){
            mBinding.imageViewBack.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_clear_to_back))
            mBinding.imageViewBack.post {
                val frameAnimation =
                    mBinding.imageViewBack.drawable as AnimatedVectorDrawable
                frameAnimation.start()
            }
        }else{
            mBinding.imageViewBack.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_back_to_clear))
            mBinding.imageViewBack.post {
                val frameAnimation =
                    mBinding.imageViewBack.drawable as AnimatedVectorDrawable
                frameAnimation.start()
            }
        }
    }

    private fun captionClickListener(){
        mBinding.editTextCaption.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) mPickedTool = ToolType.TEXT_CAPTION
        }
        mBinding.editTextCaption.setOnClickListener {
            mPickedTool = ToolType.TEXT_CAPTION
        }
    }

    private fun colorPicker(){
        mBinding.colorPicker.resetToDefault()
        mBinding.colorPicker.setOnColorChangeListener(mOnColorChangeListener)
    }

    private var mOnColorChangeListener =
        VerticalSlideColorPicker.OnColorChangeListener {
            if (it != 0) mPicketColor = it

            if (mPickedTool == ToolType.TEXT || mPickedTool == ToolType.TEXT_EDIT){
                mBinding.editText.setTextColor(mPicketColor)
                mBinding.imageViewText.background = Utils.tintDrawable(requireContext(), R.drawable.shape_circle, mPicketColor)
            }else if(mPickedTool == ToolType.BRUSH) {
                val event = GlobalEventListener(ADD_BRUSH_ACTION)
                event.mTextColor = mPicketColor
                EventBus.getDefault().post(event)
            }
        }

    fun editDone(){

        val mMediaPreviewList = mViewModel.mMediaPreviewList

        if (mViewModel.mEditOptions?.showCaption != null && mViewModel.mEditOptions?.showCaption!! &&
            mViewModel.mEditOptions?.isCaptionCompulsory != null && mViewModel.mEditOptions?.isCaptionCompulsory!!) {
            for (i in 0 until mMediaPreviewList?.size!!) {
                if (mMediaPreviewList[i].mCaption.isEmpty()){
                    Toast.makeText(requireActivity(), "Please Enter Caption", LENGTH_SHORT).show()
                    mBinding.viewPager.currentItem = i
                    mBinding.editTextCaption.requestFocus()
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    return
                }
            }
        }

        mInputMethodManager.hideSoftInputFromWindow(mBinding.root.windowToken, 0)
        disableEnableControls(false, mBinding.rootView)
        mBinding.constraintProgress.visibility = VISIBLE

        mViewModel.mMediaFinalList = ArrayList()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                getAllImages(true)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent()
                intent.putExtra(EDITED_MEDIA_LIST, mViewModel.mMediaFinalList)
                requireActivity().setResult(RESULT_OK, intent)
                requireActivity().finish()
            }, 1000)
        }

    }

    private suspend fun getAllImages(mEditDone: Boolean){
        mViewModel.mMediaFinalList = ArrayList()
        val currentWatermarkType = mViewModel.mEditOptions?.mWatermarkType!!
        if (!mEditDone) mViewModel.mEditOptions?.mWatermarkType = WatermarkType.NONE
        withContext(Dispatchers.IO){
            mViewModel.mMediaPreviewList?.mapIndexed { i, media ->
                async(Dispatchers.IO){
                    val path = if (mMediaPreviewAdapter?.getCurrentFragment(i) is ImagePreviewFragment) {
                        val mImageFragment =
                            mMediaPreviewAdapter?.getCurrentFragment(i) as ImagePreviewFragment
                        mImageFragment.saveBitmap()
                    }else {
                        media.mMediaUri
                    }

                    val mediaFinal = MediaFinal(path)
                    mediaFinal.mCaption = media.mCaption
                    mediaFinal.mOldMediaUri = media.mOldMediaUri
                    mediaFinal.mMimeType = Utils.getMimeType(mediaFinal.mMediaUri, requireActivity())
                    println(mediaFinal.mMediaUri)
                    mViewModel.mMediaFinalList?.add(mediaFinal)
                }
            }?.awaitAll()
            mViewModel.mEditOptions?.mWatermarkType = currentWatermarkType
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
            SAVE_BITMAP_FOR_CROP_ACTION_DONE -> findNavController().navigate(R.id.action_picEditorFragment_to_cropRotateFragment)
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

        if (height > 0 && mPickedTool == ToolType.TEXT_CAPTION){
            val finalHeight = height - mBinding.recyclerViewMedia.height
            val params = mBinding.constraintLayoutCaption.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, finalHeight)
            mBinding.constraintLayoutCaption.layoutParams = params
        }else{
            mPickedTool = ToolType.NONE
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
