package com.madebysr.data_melody

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** DataMelodyPlugin */
class DataMelodyPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "data_melody")
        channel.setMethodCallHandler(this)
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
        Log.v("WAVE", "Received message: $message")
    }

    private fun onNativeMessageEncoded(c_message: ShortArray) {
        Log.v("WAVE", "Playing encoded message ..")
    }

    private fun initialize(call: MethodCall, result: Result) {
        System.loadLibrary("test-cpp")
        initNative()
    }

    private fun startSendingData(call: MethodCall, result: Result) {

    }

    private fun stopSendingData(call: MethodCall, result: Result) {

    }

    private fun startReceivingData(call: MethodCall, result: Result) {

    }

    private fun stopReceivingData(call: MethodCall, result: Result) {

    }
}
