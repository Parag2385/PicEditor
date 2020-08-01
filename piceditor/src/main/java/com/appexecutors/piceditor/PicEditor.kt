package com.appexecutors.piceditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.appexecutors.piceditor.databinding.ActivityPicEditorBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.appexecutors.piceditor.editorengine.interfaces.PermissionCallback
import com.appexecutors.piceditor.editorengine.models.MediaFinal
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDITOR_OPTIONS
import com.appexecutors.piceditor.editorengine.utils.AppConstants.INTENT_FROM_PIC_EDITOR
import com.appexecutors.piceditor.editorengine.utils.PermissionUtils
import com.appexecutors.piceditor.editorengine.utils.ToolType

class PicEditor : AppCompatActivity() {

    private lateinit var mBinding: ActivityPicEditorBinding
    private lateinit var mViewModel: PicViewModel
    private lateinit var mEditOptions: EditOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pic_editor)

        mViewModel = ViewModelProvider(this).get(PicViewModel::class.java)

        mEditOptions = intent.getSerializableExtra(EDITOR_OPTIONS) as EditOptions

    }

    fun addMoreImages() {
        mEditOptions.mAddMoreImplementedListener?.addMoreImages(this, mViewModel.mMediaFinalList!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 10) {
            val mImageList = data?.getStringArrayListExtra("PICKED_MEDIA_LIST") as ArrayList
            mImageList.map {
                var mHasOldImage = false
                mEditOptions.mSelectedImageList.map { media ->
                    if (media.mOldMediaUri == it) {
                        mHasOldImage = true
                    }
                }
                if (!mHasOldImage) mEditOptions.mSelectedImageList.add(MediaFinal(it))
            }
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                when (fragment::class.java.simpleName) {
                    PicEditorFragment::class.java.simpleName -> {
                        val mPicEditor = fragment as PicEditorFragment
                        mPicEditor.mEditOptions = mEditOptions
                        mPicEditor.initAll()
                    }
                }
            }
        }
    }

    companion object {

        const val REQUEST_CODE_EDITOR = 1199

        @JvmStatic
        fun startEditing(fragment: Fragment, options: EditOptions) {
            PermissionUtils.checkForCameraWritePermissions(fragment, object : PermissionCallback {
                override fun onPermission(approved: Boolean) {
                    val mPicEditorIntent = Intent(fragment.activity, PicEditor::class.java)
                    mPicEditorIntent.putExtra(EDITOR_OPTIONS, options)
                    fragment.startActivityForResult(mPicEditorIntent, REQUEST_CODE_EDITOR)
                }
            })
        }

        @JvmStatic
        fun startEditing(activity: FragmentActivity, options: EditOptions) {
            PermissionUtils.checkForCameraWritePermissions(activity, object : PermissionCallback {
                override fun onPermission(approved: Boolean) {
                    val mPicEditorIntent = Intent(activity, PicEditor::class.java)
                    mPicEditorIntent.putExtra(EDITOR_OPTIONS, options)
                    activity.startActivityForResult(mPicEditorIntent, REQUEST_CODE_EDITOR)
                }
            })
        }
    }

    override fun onBackPressed() {
        val navHost = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                when (fragment::class.java.simpleName) {
                    PicEditorFragment::class.java.simpleName -> {
                        val mPicEditor = fragment as PicEditorFragment
                        if (mPicEditor.mPickedTool == ToolType.BRUSH) {
                            mPicEditor.brushDone()
                            mPicEditor.animateBackIcon(true)
                            mPicEditor.mPickedTool = ToolType.NONE
                        } else if (mPicEditor.mPickedTool == ToolType.TEXT || mPicEditor.mPickedTool == ToolType.TEXT_EDIT) {
                            mPicEditor.clearText(INTENT_FROM_PIC_EDITOR)
                            mPicEditor.animateBackIcon(true)
                            mPicEditor.mPickedTool = ToolType.NONE
                        } else if (mPicEditor.mPickedTool == ToolType.NONE) {
                            mViewModel.mMediaPreviewList = null
                            mViewModel.mMediaFinalList = null
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }
        }

    }
}
