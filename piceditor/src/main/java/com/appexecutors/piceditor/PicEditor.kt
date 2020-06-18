package com.appexecutors.piceditor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.appexecutors.piceditor.databinding.ActivityPicEditorBinding
import com.appexecutors.piceditor.editorengine.interfaces.PermissionCallback
import com.appexecutors.piceditor.editorengine.utils.AppConstants.EDITOR_OPTIONS
import com.appexecutors.piceditor.editorengine.utils.AppConstants.INTENT_FROM_PIC_EDITOR
import com.appexecutors.piceditor.editorengine.utils.PermissionUtils
import com.appexecutors.piceditor.editorengine.utils.ToolType

class PicEditor : AppCompatActivity(){

    private lateinit var mBinding: ActivityPicEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pic_editor)
    }

    companion object{

        @JvmStatic
        fun startEditing(fragment: Fragment, options: EditOptions){
            PermissionUtils.checkForCameraWritePermissions(fragment, object : PermissionCallback{
                override fun onPermission(approved: Boolean) {
                    val mPicEditorIntent = Intent(fragment.activity, PicEditor::class.java)
                    mPicEditorIntent.putExtra(EDITOR_OPTIONS, options)
                    fragment.startActivityForResult(mPicEditorIntent, options.mRequestCode)
                }
            })
        }

        @JvmStatic
        fun startEditing(activity: FragmentActivity, options: EditOptions){
            PermissionUtils.checkForCameraWritePermissions(activity, object : PermissionCallback{
                override fun onPermission(approved: Boolean) {
                    val mPicEditorIntent = Intent(activity, PicEditor::class.java)
                    mPicEditorIntent.putExtra(EDITOR_OPTIONS, options)
                    activity.startActivityForResult(mPicEditorIntent, options.mRequestCode)
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
                        if (mPicEditor.mPickedTool == ToolType.BRUSH){
                            mPicEditor.brushDone()
                            mPicEditor.animateBackIcon(true)
                        }else if (mPicEditor.mPickedTool == ToolType.TEXT || mPicEditor.mPickedTool == ToolType.TEXT_EDIT){
                            mPicEditor.clearText(INTENT_FROM_PIC_EDITOR)
                            mPicEditor.animateBackIcon(true)
                        }else if (mPicEditor.mPickedTool == ToolType.NONE){
                            super.onBackPressed()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }
        }

    }
}
