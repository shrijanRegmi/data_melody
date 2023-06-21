package com.madebysr.data_melody

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** DataMelodyPlugin */
class DataMelodyPlugin: FlutterPlugin, MethodCallHandler {

    private lateinit var channel : MethodChannel
    
    private lateinit var ecReceivedData: EventChannel
    private lateinit var ecIsSendingData: EventChannel
    private lateinit var ecIsReceivingData: EventChannel
    
    private var esReceivedData: EventSink? = null
    private var esIsSendingData: EventSink? = null
    private var esIsReceivingData: EventSink? = null

    private var mCapturingThread: CapturingThread? = null
    private var mPlaybackThread: PlaybackThread? = null

    private var isSendingData: Boolean = false
    private var isReceivingData: Boolean = false

    private val uiThreadHandler: Handler = Handler(Looper.getMainLooper())
    private val isReceivingHandler: Handler = Handler(Looper.getMainLooper())
    private val isSendingHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var context: Context
    private lateinit var audioManager: AudioManager


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext

        channel = MethodChannel(
            flutterPluginBinding.binaryMessenger,
            "data_melody"
        )
        ecReceivedData = EventChannel(
            flutterPluginBinding.binaryMessenger,
            "event_channels/received_data_event_channel"
        )
        ecIsSendingData = EventChannel(
            flutterPluginBinding.binaryMessenger,
            "event_channels/is_sending_data_event_channel"
        )
        ecIsReceivingData = EventChannel(
            flutterPluginBinding.binaryMessenger,
            "event_channels/is_receiving_data_event_channel"
        )

        channel.setMethodCallHandler(this)
        ecReceivedData.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventSink?) {
                esReceivedData = events
                esReceivedData?.success(hashMapOf<String, Any>())
            }

            override fun onCancel(arguments: Any?) {
                esReceivedData = null
            }
        })
        ecIsSendingData.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventSink?) {
                esIsSendingData = events
                esIsSendingData?.success(false)
                isSendingHandler.post(isSendingRunner)
            }

            override fun onCancel(arguments: Any?) {
                esIsSendingData = null
                isSendingHandler.removeCallbacks(isSendingRunner)
            }
        })
        ecIsReceivingData.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventSink?) {
                esIsReceivingData = events
                esIsReceivingData?.success(false)
                isReceivingHandler.post(isReceivingRunner)
            }

            override fun onCancel(arguments: Any?) {
                esIsReceivingData = null
                isReceivingHandler.removeCallbacks(isReceivingRunner)
            }
        })
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
            esReceivedData?.success(hashMapOf("data" to message))
        }
    }

    private fun onNativeMessageEncoded(c_message: ShortArray) {
        mPlaybackThread = PlaybackThread(c_message, object : PlaybackListener {
            override fun onProgress(progress: Int) {
                Log.d("PROGRESS", progress.toString())
            }

            override fun onCompletion() {

            }
        })
    }


    private val isReceivingRunner: Runnable = object: Runnable {
        override fun run() {
            val isRecordingOn = mCapturingThread?.capturing() == true
            if(isReceivingData != isRecordingOn) {
                isReceivingData = isRecordingOn
                esIsReceivingData?.success(mCapturingThread?.capturing() == true)
            }
            isReceivingHandler.postDelayed(this, 200)
        }
    }

    private val isSendingRunner: Runnable = object: Runnable {
        override fun run() {
            val isPlayerOn = mPlaybackThread?.playing() == true
            if(isSendingData != isPlayerOn) {
                isSendingData = isPlayerOn
                esIsSendingData?.success(mPlaybackThread?.playing() == true)
            }
            isSendingHandler.postDelayed(this, 200)
        }
    }

    private fun initialize(call: MethodCall, result: Result) {
        System.loadLibrary("test-cpp")
        initNative()

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        result.success(null)
    }

    private fun startSendingData(call: MethodCall, result: Result) {
        mPlaybackThread?.let {
            if(it.playing()) {
                result.success(null)
                return
            }
        }

        val data = call.argument<String>("data")
        val player = call.argument<String>("player")
        val volume = call.argument<Int>("volume") ?: 50

        setDeviceVolume(volume)

        val txProtocolId = getTxProtocolId(player!!)
        sendMessage(data!!, txProtocolId)

        mPlaybackThread?.startPlayback()

        result.success(null)
    }

    private fun stopSendingData(call: MethodCall, result: Result) {
        mPlaybackThread?.stopPlayback()

        result.success(null)
    }

    private fun startReceivingData(call: MethodCall, result: Result) {
        mCapturingThread = CapturingThread(object : AudioDataReceivedListener {
            override fun onAudioDataReceived(data: ShortArray) {
                processCaptureData(data)
            }
        })

        if(mCapturingThread?.capturing() == false) {
            mCapturingThread?.startCapturing()
        }

        result.success(null)
    }

    private fun stopReceivingData(call: MethodCall, result: Result) {
        mCapturingThread?.stopCapturing()

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

    private fun setDeviceVolume(volume: Int) {
        val requiredVol = (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100.0) * volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, requiredVol.toInt(), 0)
    }
}


