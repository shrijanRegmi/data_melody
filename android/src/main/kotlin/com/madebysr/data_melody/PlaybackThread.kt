package com.madebysr.data_melody

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.nio.ShortBuffer


internal interface PlaybackListener {
    fun onProgress(progress: Int)
    fun onCompletion()
}

internal class PlaybackThread(
    samples: ShortArray,
    listener: PlaybackListener?
) {
    private var mThread: Thread? = null
    private var mShouldContinue = false
    private val mSamples: ShortBuffer

    private val mNumSamples: Int
    private val mListener: PlaybackListener?

    init {
        mSamples = ShortBuffer.wrap(samples)
        mNumSamples = samples.size
        mListener = listener
    }

    fun playing(): Boolean {
        return mThread != null
    }


    fun startPlayback() {
        if (mThread != null) return

        // Start streaming in a thread
        mShouldContinue = true
        mThread = Thread { play() }
        mThread!!.start()
    }

    fun stopPlayback() {
        if (mThread == null) return
        mShouldContinue = false
        mThread = null
    }

    private fun play() {
        val bufferSize: Int = 16 * 1024

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack.setPlaybackPositionUpdateListener(object :
            AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onPeriodicNotification(track: AudioTrack) {
                if (mListener != null && track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    mListener.onProgress(track.playbackHeadPosition * 1000 / SAMPLE_RATE)
                }
            }

            override fun onMarkerReached(track: AudioTrack) {
                Log.v(LOG_TAG, "Audio file end reached")
                track.release()
                mListener?.onCompletion()
            }
        })

        audioTrack.positionNotificationPeriod = SAMPLE_RATE / 30 // 30 times per second
        audioTrack.notificationMarkerPosition = mNumSamples
        audioTrack.play()

        Log.v(LOG_TAG, "Audio streaming started")

        val buffer = ShortArray(bufferSize)
        mSamples.rewind()
        val limit = mNumSamples
        var totalWritten = 0

        while (mSamples.position() < limit && mShouldContinue) {
            val numSamplesLeft = limit - mSamples.position()
            var samplesToWrite: Int
            if (numSamplesLeft >= buffer.size) {
                mSamples[buffer]
                samplesToWrite = buffer.size
            } else {
                for (i in numSamplesLeft until buffer.size) {
                    buffer[i] = 0
                }
                mSamples[buffer, 0, numSamplesLeft]
                samplesToWrite = numSamplesLeft
            }
            totalWritten += samplesToWrite
            audioTrack.write(buffer, 0, samplesToWrite)
        }

        if (!mShouldContinue) {
            audioTrack.release()
        }

        Log.v(LOG_TAG, "Audio streaming finished. Samples written: $totalWritten")
    }

    companion object {
        const val SAMPLE_RATE = 48000
        private val LOG_TAG = PlaybackThread::class.java.simpleName
    }
}