package com.appexecutors.piceditor.editorengine.preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.appexecutors.piceditor.editorengine.models.MediaPreview
import com.appexecutors.piceditor.editorengine.utils.AppConstants.MEDIA_POSITION

class MediaPreviewPagerAdapter(
    fm: FragmentActivity,
    private val mMediaList: ArrayList<MediaPreview>
) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return mMediaList.size
    }

    override fun createFragment(position: Int): Fragment {
        val mBundle = Bundle()
        mBundle.putInt(MEDIA_POSITION, position)
        val imageFragment =
            ImagePreviewFragment()
        imageFragment.arguments = mBundle
        return imageFragment
    }
}