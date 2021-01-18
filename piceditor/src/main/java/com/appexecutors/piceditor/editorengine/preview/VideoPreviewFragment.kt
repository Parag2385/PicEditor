package com.appexecutors.piceditor.editorengine.preview

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.appexecutors.piceditor.R
import com.appexecutors.piceditor.databinding.FragmentVideoPreviewBinding
import com.appexecutors.piceditor.editorengine.PicViewModel
import com.appexecutors.piceditor.editorengine.utils.AppConstants.MEDIA_POSITION
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 * A simple [Fragment] subclass.
 */
class VideoPreviewFragment : Fragment() {

    private lateinit var mBinding: FragmentVideoPreviewBinding

    private lateinit var mViewModel: PicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_preview, container, false)
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
            initializePlayer(mViewModel.mMediaPreviewList!![mCurrentPosition].mMediaUri)
        }

    }

    private var player: SimpleExoPlayer? = null

    private fun initializePlayer(mImageUri: String?) {
        if (player == null) { // 1. Create a default TrackSelector
            val loadControl: LoadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(32*1024, 64*1024, 1024, 1024).createDefaultLoadControl()

            val videoTrackSelectionFactory: TrackSelection.Factory =
                AdaptiveTrackSelection.Factory()
            val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            // 2. Create the player
            player = ExoPlayerFactory.newSimpleInstance(
                mBinding.root.context, trackSelector,
                loadControl
            )
            mBinding.videoFullScreenPlayer.player = player

            buildMediaSource(Uri.parse(mImageUri))
        }
    }

    private fun buildMediaSource(mUri: Uri) {

        val eventListener = object : Player.EventListener{

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        mBinding.spinnerVideoDetails.visibility = View.VISIBLE
                    }
                    Player.STATE_ENDED -> {
                    }
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_READY -> {
                        mBinding.spinnerVideoDetails.visibility = View.GONE
                    }
                    else -> {
                    }
                }
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {/*Not Required*/ }

            override fun onSeekProcessed() {/*Not Required*/ }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {/*Not Required*/ }

            override fun onPlayerError(error: ExoPlaybackException?) {/*Not Required*/ }

            override fun onLoadingChanged(isLoading: Boolean) {/*Not Required*/ }

            override fun onPositionDiscontinuity(reason: Int) {/*Not Required*/ }

            override fun onRepeatModeChanged(repeatMode: Int) {/*Not Required*/ }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {/*Not Required*/ }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {/*Not Required*/ }

        }

        // Measures bandwidth during playback. Can be null if not required.
        val bandwidthMeter = DefaultBandwidthMeter()
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            mBinding.root.context,
            Util.getUserAgent(mBinding.root.context, getString(R.string.app_name)), bandwidthMeter
        )
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mUri)
        // Prepare the player with the source.
        player?.prepare(videoSource)
        player?.playWhenReady = true
        player?.addListener(eventListener)
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

    private fun pausePlayer() {
        if (player != null) {
            player?.playWhenReady = false
            player?.playbackState
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

}
