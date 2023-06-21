package com.madebysr.data_melody

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** DataMelodyPlugin */
class DataMelodyPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel
    private lateinit var mCapturingThread: CapturingThread
    private lateinit var mPlaybackThread: PlaybackThread
    private lateinit var context: Context

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "data_melody")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "initialize" -> initialize(call, result)
            "startSendingData" -> startSendingData(call, result)
            "stopSendingData" -> stopSendingData(call, result)
            "startReceivingData" -> startReceivingData(call, result)
            "stopReceivingData" -> stopReceivingData(call, result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    // Native interface:
    private external fun initNative()
    private external fun processCaptureData(data: ShortArray)
    private external fun sendMessage(message: String)

    // Native callbacks:
    private fun onNativeMessageReceived(c_message: ByteArray) {
        val message = String(c_message)
    }

    private fun onNativeMessageEncoded(c_message: ShortArray) {
        mPlaybackThread = PlaybackThread(c_message, object : PlaybackListener {
            override fun onProgress(progress: Int) {}

            override fun onCompletion() {
                mPlaybackThread.stopPlayback()
            }
        })
    }

    private fun initialize(call: MethodCall, result: Result) {
        System.loadLibrary("test-cpp")
        initNative()

        result.success(null)
    }

    private fun startSendingData(call: MethodCall, result: Result) {
        val data = call.argument<String>("data")

        sendMessage(data!!)
        mPlaybackThread.startPlayback()

        result.success(null)
    }

    private fun stopSendingData(call: MethodCall, result: Result) {
        mPlaybackThread.stopPlayback()

        result.success(null)
    }

    private fun startReceivingData(call: MethodCall, result: Result) {
        mCapturingThread = CapturingThread(object : AudioDataReceivedListener {
            override fun onAudioDataReceived(data: ShortArray) {
                processCaptureData(data)
            }
        })

        if(!mCapturingThread.capturing()) {
            mCapturingThread.startCapturing()
        }

        result.success(null)
    }

    private fun stopReceivingData(call: MethodCall, result: Result) {
        mCapturingThread.stopCapturing()

        result.success(null)
    }
}


