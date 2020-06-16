package com.appexecutors.piceditorsample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appexecutors.piceditor.EditOptions
import com.appexecutors.piceditor.PicEditor
import com.appexecutors.piceditor.editorengine.AddMoreImagesListener
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
            Pix.start(this, Options.init().setRequestCode(PIX_IMAGE_PICKER_CODE).setCount(5))
        }

        mEditOptions = EditOptions.init().apply {
            mRequestCode = PIC_IMAGE_EDITOR_CODE
            mAddMoreImplementedListener = mAddMoreListener
            showCaption = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PIX_IMAGE_PICKER_CODE){
            mEditOptions.mSelectedImageList = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList
            PicEditor.startEditing(
                this,
                mEditOptions
            )
        }
    }

    private var mAddMoreListener = object: AddMoreImagesListener {

        override fun addMoreImages(context: AppCompatActivity, selectedImageList: ArrayList<String>, requestCodePix: Int) {
            Pix.start(context, Options.init().apply {
                requestCode = requestCodePix
                count = 5
                preSelectedUrls = selectedImageList
            })
        }
    }
}
