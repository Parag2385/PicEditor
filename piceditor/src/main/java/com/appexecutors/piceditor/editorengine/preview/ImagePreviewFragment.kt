package com.appexecutors.piceditor.editorengine.preview

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.appexecutors.piceditor.R
import com.appexecutors.piceditor.databinding.FragmentImagePreviewBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ACTION_STARTED
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ACTION_STOPPED
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ADD_BRUSH_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.ADD_TEXT_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.DISABLE_BRUSH_ACTION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDIT_TEXT_ACTION_DONE
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDIT_TEXT_ACTION_START
import com.appexecutors.piceditor.editorengine.utils.AppConstants.MEDIA_POSITION
import com.appexecutors.piceditor.editorengine.utils.AppConstants.SAVE_BITMAP_FOR_CROP_ACTION_DONE
import com.appexecutors.piceditor.editorengine.utils.AppConstants.UNDO
import com.appexecutors.piceditor.editorengine.utils.AppConstants.UNDO_REDO_ACTION
import com.appexecutors.piceditor.editorengine.utils.GlobalEventListener
import com.appexecutors.piceditor.editorengine.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ja.burhanrashid52.photoeditor.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * A simple [Fragment] subclass.
 */
class ImagePreviewFragment : Fragment(), OnPhotoEditorListener {

    private lateinit var mBinding: FragmentImagePreviewBinding
    private lateinit var mViewModel: PicViewModel

    private var mPhotoEditor: PhotoEditor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_image_preview, container, false)
        return mBinding.root
    }

    private var mCurrentPosition = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = requireActivity().let {
            ViewModelProvider(requireActivity()).get(PicViewModel::class.java)
        }

        mCurrentPosition = arguments?.getInt(MEDIA_POSITION)!!

        if (mViewModel.mMediaPreviewList != null){
            val mProcessedBitmap = mViewModel.mMediaPreviewList!![mCurrentPosition].mProcessedBitmap
            if (mProcessedBitmap != null){
                mBinding.photoEditorView.source.setImageBitmap(mProcessedBitmap)
            }else{
                loadImage(mViewModel.mMediaPreviewList!![mCurrentPosition].mMediaUri)
            }
        }

        if (mPhotoEditor == null) {

            mPhotoEditor = PhotoEditor.Builder(requireContext(), mBinding.photoEditorView)
                .setPinchTextScalable(true)
                .build() // build photo editor sdk


            mPhotoEditor?.setOnPhotoEditorListener(this)
        }

    }

    private fun loadImage(mImageUri: String?){
        Glide.with(this)
            .setDefaultRequestOptions(RequestOptions())
            .load(mImageUri)
            .into(mBinding.photoEditorView.source)

        //todo: provision to add watermark
    }

    private fun undo() = mPhotoEditor?.undo()

    private fun redo() = mPhotoEditor?.redo()

    private val saveSettings = SaveSettings.Builder()
        .setClearViewsEnabled(true)
        .setTransparencyEnabled(true)
        .setCompressFormat(Bitmap.CompressFormat.JPEG)
        .setCompressQuality(70)
        .build()

    fun saveSingleBitmap(){

        mPhotoEditor?.saveAsBitmap(object: OnSaveBitmap{
            override fun onFailure(e: Exception?) {/*Not Required*/}

            override fun onBitmapReady(saveBitmap: Bitmap?) {
                mViewModel.mMediaPreviewList!![mCurrentPosition].mProcessedBitmap = saveBitmap
                EventBus.getDefault().post(GlobalEventListener(SAVE_BITMAP_FOR_CROP_ACTION_DONE))
            }
        })
    }

    suspend fun saveBitmap(): String =
        suspendCoroutine {
            mPhotoEditor?.saveAsBitmap(saveSettings, object: OnSaveBitmap{
                override fun onFailure(e: Exception?) {/*Not Required*/}

                override fun onBitmapReady(saveBitmap: Bitmap?) {
                    Log.e("ImagePreviewFragment", "onBitmapReady: ")
                    val mFilePath = Utils.saveImage(saveBitmap!!, requireActivity())
                    it.resume(mFilePath!!)
                }
            })
        }
    
    @Subscribe
    fun onGlobalEventListener(mEvent: GlobalEventListener){

        if (mCurrentPosition != mViewModel.mCurrentMediaPosition) return

        when (mEvent.mActionID) {
            ADD_TEXT_ACTION -> {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(mEvent.mTextColor)
                styleBuilder.withTextSize(30f)
                mPhotoEditor?.addText(mEvent.mText, styleBuilder)
            }
            ADD_BRUSH_ACTION -> {
                mPhotoEditor?.setBrushDrawingMode(true)
                mPhotoEditor?.brushSize = 13f
                mPhotoEditor?.brushColor = mEvent.mTextColor
            }
            DISABLE_BRUSH_ACTION ->{
                mPhotoEditor?.setBrushDrawingMode(false)
            }
            EDIT_TEXT_ACTION_DONE ->{
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(mEvent.mTextColor)
                if (mClickedTextView != null)
                mPhotoEditor?.editText(mClickedTextView!!, mEvent.mText, styleBuilder)
            }
            UNDO_REDO_ACTION ->{
                if (mEvent.mUndoRedoFlag == UNDO) undo() else redo()
            }
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

    private var mClickedTextView: View? = null

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        mClickedTextView = rootView
        val event = GlobalEventListener(EDIT_TEXT_ACTION_START)
        event.mText = text!!
        event.mTextColor = colorCode
        EventBus.getDefault().post(event)
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        EventBus.getDefault().post(GlobalEventListener(ACTION_STARTED))
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        //
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        //
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        EventBus.getDefault().post(GlobalEventListener(ACTION_STOPPED))
    }
}
