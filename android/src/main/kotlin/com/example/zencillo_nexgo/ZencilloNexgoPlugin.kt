package com.example.zencillo_nexgo

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class ZencilloNexgoPlugin :
    FlutterPlugin,
    MethodCallHandler,
    ActivityAware {

    private lateinit var channel: MethodChannel
    private lateinit var applicationContext: Context
    private var activity: Activity? = null

    private val nexgo = NexGoPlugin()

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zencillo_nexgo")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(
        call: MethodCall,
        result: Result
    ) {
        when (call.method) {

            "print" -> {
                val raw = call.argument<String>("text") ?: ""
                val linkQr = call.argument<String>("code") ?: ""
                val isQr = call.argument<Boolean>("isQr") ?: false

                val currentContext = activity ?: applicationContext

                val (response, message) = nexgo.print(
                    text = raw,
                    context = currentContext,
                    linkQr = linkQr,
                    isQr = isQr
                )

                if (response) {
                    result.success(message)
                } else {
                    result.error(
                        "NEXGO_PRINT_ERROR",
                        message,
                        null
                    )
                }
            }

            "scan" -> {
                val currentActivity = activity

                if (currentActivity == null) {
                    result.error(
                        "NEXGO_SCAN_ERROR",
                        "No hay Activity disponible para abrir el scanner NexGo.",
                        null
                    )
                    return
                }

                nexgo.scan(currentActivity, result)
            }

            "nfc" -> {
                val currentContext = activity ?: applicationContext

                val (response, message) = nexgo.nfc(currentContext)

                if (response) {
                    result.success(message)
                } else {
                    result.error(
                        "NEXGO_NFC_ERROR",
                        message,
                        null
                    )
                }
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}