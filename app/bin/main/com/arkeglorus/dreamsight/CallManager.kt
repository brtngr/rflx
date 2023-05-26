package com.arkeglorus.dreamsight

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.telecom.TelecomManager
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_HEADSETHOOK
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


class CallManager(private val context: Context) {
    private val audioManager: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

    fun acceptCall() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                throughTelephonyService(context)
            } else {
                throughMediaController(context)
            }
        } catch (e: Exception) {
            throughReceiver(context)
        }
    }

    fun closeCall() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                closeThroughTelephonyService(context)
            } else {
                throughMediaController(context)
            }
        } catch (e: Exception) {
            throughReceiver(context)
        }
    }

    fun enableSpeakerPhone() {
        GlobalScope.launch {
            delay(1000)
            audioManager.mode = AudioManager.MODE_IN_CALL
            if (!audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = true
            }
        }
    }

    @SuppressLint("NewApi")
    private fun closeThroughTelephonyService(context: Context) {
        val tm = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            tm.endCall()
        }
    }

    @SuppressLint("NewApi")
    private fun throughTelephonyService(context: Context) {
        val tm = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            tm.acceptRingingCall()
        }
    }

    private fun throughAudioManager() {
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_HEADSETHOOK)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, KEYCODE_HEADSETHOOK)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    private fun throughReceiver(context: Context) {
        try {
            throughTelephonyService(context)
        } catch (exception: Exception) {
            val broadcastConnected = "HTC".equals(
                Build.MANUFACTURER,
                ignoreCase = true
            ) && !audioManager.isWiredHeadsetOn

            if (broadcastConnected) {
                broadcastHeadsetConnected(false, context)
            }
            try {
                Runtime.getRuntime().exec("input keyevent $KEYCODE_HEADSETHOOK")
            } catch (ioe: IOException) {
                throughPhoneHeadsetHook(context)
            } finally {
                if (broadcastConnected) {
                    broadcastHeadsetConnected(false, context)
                }
            }
        }

    }

    private fun broadcastHeadsetConnected(connected: Boolean, context: Context) {
        val intent = Intent(Intent.ACTION_HEADSET_PLUG)
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
        intent.putExtra("state", if (connected) 1 else 0)
        intent.putExtra("name", "mysms")
        try {
            context.sendOrderedBroadcast(intent, null)
        } catch (e: Exception) {
        }

    }

    private fun throughMediaController(context: Context) {
        val mediaSessionManager =
            context.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        try {
            val controllers = mediaSessionManager.getActiveSessions(
                ComponentName(
                    context,
                    NotificationListenerService::class.java
                )
            )
            for (controller in controllers) {
                if ("com.android.server.telecom" == controller.packageName) {
                    controller.dispatchMediaButtonEvent(
                        KeyEvent(
                            KeyEvent.ACTION_UP,
                            KEYCODE_HEADSETHOOK
                        )
                    )
                    break
                }
            }
        } catch (e: Exception) {
            throughAudioManager()
        }

    }

    private fun throughPhoneHeadsetHook(context: Context) {
        val buttonDown = Intent(Intent.ACTION_MEDIA_BUTTON)
        buttonDown.putExtra(
            Intent.EXTRA_KEY_EVENT,
            KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_HEADSETHOOK)
        )
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED")

        val buttonUp = Intent(Intent.ACTION_MEDIA_BUTTON)
        buttonUp.putExtra(
            Intent.EXTRA_KEY_EVENT,
            KeyEvent(KeyEvent.ACTION_UP, KEYCODE_HEADSETHOOK)
        )
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED")
    }

    companion object {
        private val TAG = CallManager::class.java.simpleName
    }
}