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
import com.appexecutors.piceditorsample.AppConstants.PIC_IMAGE_EDITOR_CODE
import com.appexecutors.piceditorsample.AppConstants.PIX_IMAGE_PICKER_CODE
import com.appexecutors.piceditorsample.databinding.ActivityMainBinding
import com.fxn.pix.Options
import com.fxn.pix.Pix

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mEditOptions: EditOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mBinding.fab.setOnClickListener {
            Pix.start(this, Options.init().setRequestCode(PIX_IMAGE_PICKER_CODE).setCount(10))
        }

        mEditOptions = EditOptions.init().apply {
            mRequestCode = PIC_IMAGE_EDITOR_CODE
            mAddMoreImplementedListener = mAddMoreListener
            showCaption = true
            isCaptionCompulsory = true
            mWatermarkType = WatermarkType.DATE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PIX_IMAGE_PICKER_CODE){
            val mImageList = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList
            mImageList.map {
                mEditOptions.mSelectedImageList.add(MediaFinal(it))
            }
            PicEditor.startEditing(
                this,
                mEditOptions
            )
        }

        if (resultCode == Activity.RESULT_CANCELED && requestCode == mEditOptions.mRequestCode) mEditOptions.mSelectedImageList = ArrayList()

        if (resultCode == Activity.RESULT_OK && requestCode == mEditOptions.mRequestCode){
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
            selectedImageList: ArrayList<MediaFinal>,
            requestCodePix: Int
        ) {
            val mImagePathList = ArrayList<String>()
            selectedImageList.map { mImagePathList.add(it.mOldMediaUri) }
            Pix.start(context, Options.init().apply {
                requestCode = requestCodePix
                count = 5
                preSelectedUrls = mImagePathList
            })
        }
    }
}
