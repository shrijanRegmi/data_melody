package com.madebysr.data_melody

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.logging.StreamHandler

/** DataMelodyPlugin */
class DataMelodyPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private lateinit var channel : MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventSink? = null

    private lateinit var mCapturingThread: CapturingThread
    private lateinit var mPlaybackThread: PlaybackThread
    private lateinit var context: Context

    private val uiThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "data_melody")
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "data_melody_event_channel")

        channel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)
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
    private external fun sendMessage(message: String, txProtocolId: Int)

    // Native callbacks:
    private fun onNativeMessageReceived(c_message: ByteArray) {
        val message = String(c_message)
        uiThreadHandler.post {
            eventSink?.success(hashMapOf("data" to message))
        }
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
        val player = call.argument<String>("player")

        val txProtocolId = getTxProtocolId(player!!)

        sendMessage(data!!, txProtocolId)
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

    private fun getTxProtocolId(player: String) : Int {
        return when (player) {
            "ultrasonicNormal" -> 3
            "ultrasonicFast" -> 4
            "ultrasonicFastest" -> 5
            "audibleNormal" -> 0
            "audibleFast" -> 1
            "audibleFastest" -> 2
            "dtNormal" -> 6
            "dtFast" -> 7
            "dtFastest" -> 8
            else -> 3
        }
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        eventSink = events
        eventSink?.success(hashMapOf<String, Any>())
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }
}


