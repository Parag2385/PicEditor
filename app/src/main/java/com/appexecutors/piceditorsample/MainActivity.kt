package com.appexecutors.piceditorsample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appexecutors.piceditor.EditOptions
import com.appexecutors.piceditor.PicEditor
import com.appexecutors.piceditor.editorengine.AddMoreImagesListener
import com.appexecutors.piceditor.editorengine.models.MediaFinal
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDITED_MEDIA_LIST
import com.appexecutors.piceditor.editorengine.utils.WatermarkType
import com.appexecutors.piceditorsample.databinding.ActivityMainBinding
import com.appexecutors.picker.Picker
import com.appexecutors.picker.Picker.Companion.PICKED_MEDIA_LIST
import com.appexecutors.picker.Picker.Companion.REQUEST_CODE_PICKER
import com.appexecutors.picker.utils.PickerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mEditOptions: EditOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val pickerOptions = PickerOptions.init().apply {
            maxCount = 10
            maxVideoDuration = 60
        }

        mBinding.fab.setOnClickListener {
            Picker.startPicker(this, pickerOptions)
        }

        mEditOptions = EditOptions.init().apply {
            mAddMoreImplementedListener = mAddMoreListener
            showCaption = true
            isCaptionCompulsory = true
            mWatermarkType = WatermarkType.DATE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICKER){
            val mImageList = data?.getStringArrayListExtra(PICKED_MEDIA_LIST) as ArrayList
            mImageList.map {
                mEditOptions.mSelectedImageList.add(MediaFinal(it))
            }
            PicEditor.startEditing(
                this,
                mEditOptions
            )
        }

        if (resultCode == Activity.RESULT_CANCELED && requestCode == PicEditor.REQUEST_CODE_EDITOR) mEditOptions.mSelectedImageList = ArrayList()

        if (resultCode == Activity.RESULT_OK && requestCode == PicEditor.REQUEST_CODE_EDITOR){
            @Suppress("UNCHECKED_CAST")
            val mEditedList = data?.getSerializableExtra(EDITED_MEDIA_LIST) as ArrayList<MediaFinal>?

            if (mEditedList != null) {
                for (i in 0 until mEditedList.size) {
                    Log.e("MainActivity", "onActivityResult: ${mEditedList[i].mMediaUri}")
                }
            }

            mEditOptions.mSelectedImageList = ArrayList()
        }
    }

    private var mAddMoreListener = object: AddMoreImagesListener {

        override fun addMoreImages(
            context: AppCompatActivity,
            selectedImageList: ArrayList<MediaFinal>
        ) {
            val mImagePathList = ArrayList<String>()
            selectedImageList.map { mImagePathList.add(it.mOldMediaUri) }
            val pickerOptions = PickerOptions.init().apply {
                maxCount = 10
                maxVideoDuration = 60
                preSelectedMediaList = mImagePathList
            }
            Picker.startPicker(context, pickerOptions)
        }
    }
}
