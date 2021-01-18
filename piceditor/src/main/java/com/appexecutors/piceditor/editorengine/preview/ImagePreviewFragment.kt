package com.appexecutors.piceditor.editorengine.preview

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
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
import com.appexecutors.piceditor.editorengine.utils.WatermarkType
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ja.burhanrashid52.photoeditor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.coroutines.Continuation
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
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO){
                mPhotoEditor?.saveAsBitmap(object : OnSaveBitmap {
                    override fun onFailure(e: Exception?) {/*Not Required*/
                    }

                    override fun onBitmapReady(saveBitmap: Bitmap?) {
                        mViewModel.mMediaPreviewList?.let {
                            it[mCurrentPosition].mProcessedBitmap = saveBitmap
                            EventBus.getDefault().post(GlobalEventListener(SAVE_BITMAP_FOR_CROP_ACTION_DONE))
                        }
                    }
                })
            }
        }
    }

    suspend fun saveBitmap(): String =
        suspendCoroutine {

            CoroutineScope(Dispatchers.Main).launch {

                try {
                    val saveBitmap = withContext(Dispatchers.IO){
                        if (mBinding.photoEditorView.source?.drawable != null)
                            (mBinding.photoEditorView.source?.drawable as BitmapDrawable).bitmap
                        else {
                            getImageTheOtherWay(it)
                            null
                        }
                    }

                    if (saveBitmap != null) saveImage(it, saveBitmap)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }

    private fun getImageTheOtherWay(it: Continuation<String>){
        mPhotoEditor?.saveAsBitmap(saveSettings, object: OnSaveBitmap{
            override fun onFailure(e: Exception?) {/*Not Required*/}

            override fun onBitmapReady(saveBitmap: Bitmap?) {
                CoroutineScope(Dispatchers.Main).launch {
                    saveImage(it, saveBitmap)
                }
            }
        })
    }

    private suspend fun saveImage(it: Continuation<String>, saveBitmap: Bitmap?){

        if (saveBitmap == null) {
            Toast.makeText(requireActivity(), "Cannot Save Image, Please Try again", LENGTH_SHORT).show()
            return
        }

        when (mViewModel.mEditOptions?.mWatermarkType) {
            WatermarkType.NONE -> {
                val mFilePath = withContext(Dispatchers.IO) {
                    Utils.saveImage(saveBitmap, requireActivity())
                }
                it.resume(mFilePath!!)
            }
            WatermarkType.DATE -> {
                val stringDate = Utils.getStringDate("dd/MM/yyyy hh:mm a", Date())
                val bitmapWithWatermark = Utils.waterMark(saveBitmap, stringDate, Color.BLACK, 0, 10f)
                val mFilePath = withContext(Dispatchers.IO) {
                    Utils.saveImage(bitmapWithWatermark!!, requireActivity())
                }
                it.resume(mFilePath!!)
            }
            WatermarkType.TEXT -> {
                val bitmapWithWatermark = Utils.waterMark(saveBitmap, mViewModel.mEditOptions?.mWatermarkText, Color.BLACK, 0, 10f)
                val mFilePath = withContext(Dispatchers.IO) {
                    Utils.saveImage(bitmapWithWatermark!!, requireActivity())
                }
                it.resume(mFilePath!!)
            }
        }
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
