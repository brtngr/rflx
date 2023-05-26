package com.arkeglorus.dreamsight.v1

import android.app.Notification
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.arkeglorus.dreamsight.BuildConfig


class NotificationListener : NotificationListenerService() {

    object ApplicationPackageNames {
        const val TELEGRAM_PACK_NAME = "org.telegram.messenger"
        const val WHATSAPP_PACK_NAME = "com.whatsapp"
        const val GOOGLE_MAPS_PACK_NAME = "com.google.android.apps.maps"
        const val CALL_PACK_NAME = "call"
    }

    private fun logDebug(title: String, text: String?) {
        if (BuildConfig.DEBUG) {
            println("${title}: $text")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            // logDebug("package", it.packageName)
            when (it.packageName) {
                ApplicationPackageNames.WHATSAPP_PACK_NAME -> processWANotification(it)
                ApplicationPackageNames.TELEGRAM_PACK_NAME -> processWANotification(it)
                ApplicationPackageNames.GOOGLE_MAPS_PACK_NAME -> processMapsNotification(it)
                else -> return
            }
        }

        //Log.v("focus", "in onNotificationPosted() of MyNotifService");
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.v("focus", "in onListenerConnected() of MyNotifService")
    }

    override fun onListenerDisconnected() {
        Log.v("focus", "in onListenerDisconnected() of MyNotifService")
        super.onListenerDisconnected()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.v("focus", "in onBind() of MyNotifService")
        return super.onBind(intent)
    }

    override fun onDestroy() {
        Log.v("focus", "in onDestroy() of MyNotifService")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("focus", "in onStartCommand() of MyNotifService")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v("focus", "in onUnbind() of MyNotifService")
        return super.onUnbind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.v("focus", "in onTaskRemoved() of MyNotifService")
        super.onTaskRemoved(rootIntent)
    }

    override fun onLowMemory() {
        Log.v("focus", "in onLowMemory() of MyNotifService")
        super.onLowMemory()
    }

    override fun onCreate() {
        Log.v("focus", "in onCreate() of MyNotifService")
        super.onCreate()
    }

    private fun processWANotification(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        logDebug("title", title)

        if (title == "WhatsApp") return

        var text: String? = null
        if (extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
            text = extras.getCharSequence(Notification.EXTRA_TEXT)!!.toString()
            logDebug("text", text)
            if (text.toString().contains(" new messages")) {
                logDebug("terminated", text)
                return
            }
        }
        if (text == null) {
            if (extras.get(Notification.EXTRA_TEXT_LINES) != null) {
                (extras.get(Notification.EXTRA_TEXT_LINES) as? Array<*>)?.let {
                    if (it.isNotEmpty()) {
                        text = it[it.size - 1].toString()
                        logDebug("text lines", text)
                    }
                }
            }
        }

        val intent = Intent("com.arkeglorus.dreamsight")
        intent.putExtra(APPS_NAME, ApplicationPackageNames.WHATSAPP_PACK_NAME)
        intent.putExtra(WA_TITLE, title)
        intent.putExtra(WA_TEXT, text)

        sendBroadcast(intent)
    }

    private fun processMapsNotification(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val titleString = extras.getCharSequence(Notification.EXTRA_TITLE, "")
        val titles = titleString.split("-")
        val texts = extras.getCharSequence(Notification.EXTRA_TEXT, "").split("-")
        val subTexts = extras.getString(Notification.EXTRA_SUB_TEXT, "").toString().split("·")

        val intent = Intent("com.arkeglorus.dreamsight")
        intent.putExtra(APPS_NAME, ApplicationPackageNames.GOOGLE_MAPS_PACK_NAME)
        intent.putExtra(MAPS_ICON, sbn.notification.getLargeIcon())
        titles.getOrNull(0)?.let {
            intent.putExtra(MAPS_NEXT_DISTANCE, it.trim())
        }
        titles.getOrNull(1)?.let {
            intent.putExtra(MAPS_NEXT_MOVE, it.trim())
        }
        texts.getOrNull(0)?.let {
            intent.putExtra(MAPS_DESTINATION, it.trim())
        }
        subTexts.getOrNull(1)?.let {
            intent.putExtra(MAPS_DISTANCE, it.trim())
        }

        subTexts.getOrNull(0)?.let {
            intent.putExtra(MAPS_DESTINATION_ETA, it.trim())
        }
        sendBroadcast(intent)
    }

    companion object {
        // TODO: move this to separate object, and make interface to retrieve
        const val APPS_NAME = "apps.name"
        const val MAPS_ICON = "maps.icon"
        const val MAPS_NEXT_DISTANCE = "maps.next.distance"
        const val MAPS_NEXT_MOVE = "maps.next.move"
        const val MAPS_DESTINATION = "maps.destination"
        const val MAPS_DISTANCE = "maps.distance"
        const val MAPS_DESTINATION_ETA = "maps.destination.eta"
        const val WA_TITLE = "wa.title"
        const val WA_TEXT = "wa.text"
        const val CALL_NUMBER = "call.number"
        const val CALL_END = "call.end"
        const val CALL_ONGOING = "call.ongoing"
    }
}