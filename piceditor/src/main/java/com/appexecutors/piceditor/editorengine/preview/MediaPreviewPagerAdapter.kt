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

    private val mFragmentList: MutableList<Fragment> = mutableListOf()

    override fun getItemCount(): Int = mMediaList.size

    override fun createFragment(position: Int): Fragment {
        val mBundle = Bundle()
        mBundle.putInt(MEDIA_POSITION, position)
        val imageFragment =
            ImagePreviewFragment()
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

    fun getCurrentFragment(position: Int): Fragment{
        return mFragmentList[position]
    }
}