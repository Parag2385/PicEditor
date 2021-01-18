package com.appexecutors.piceditor.editorengine.preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.appexecutors.piceditor.editorengine.models.MediaPreview
import com.appexecutors.piceditor.editorengine.utils.AppConstants.MEDIA_POSITION
import com.appexecutors.piceditor.editorengine.utils.Utils

class MediaPreviewPagerAdapter(
    private val fm: FragmentActivity,
    private val mMediaList: ArrayList<MediaPreview>
) : FragmentStateAdapter(fm) {

    private val mFragmentList: MutableList<Fragment> = mutableListOf()

    override fun getItemCount(): Int = mMediaList.size

    override fun createFragment(position: Int): Fragment {
        val mBundle = Bundle()
        mBundle.putInt(MEDIA_POSITION, position)

        if (Utils.getMimeType(mMediaList[position].mMediaUri, fm) != null && Utils.getMimeType(mMediaList[position].mMediaUri, fm)?.contains("image")!!){
            val imageFragment = ImagePreviewFragment()
            imageFragment.arguments = mBundle
            mFragmentList.add(imageFragment)
            return imageFragment
        }else if (Utils.getMimeType(mMediaList[position].mMediaUri, fm) != null && Utils.getMimeType(mMediaList[position].mMediaUri, fm)?.contains("video")!!){
            val videoFragment = VideoPreviewFragment()
            videoFragment.arguments = mBundle
            mFragmentList.add(videoFragment)
            return videoFragment
        }

        val imageFragment = ImagePreviewFragment()
        imageFragment.arguments = mBundle
        mFragmentList.add(imageFragment)
        return imageFragment
    }

    private val pageIds= mMediaList.map { it.hashCode().toLong() }

    override fun getItemId(position: Int): Long {
        return mMediaList[position].hashCode().toLong() // make sure notifyDataSetChanged() works
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageIds.contains(itemId)
    }

    fun getCurrentFragment(position: Int): Fragment?{
        if (position > mFragmentList.size - 1) return null
        return mFragmentList[position]
    }
}