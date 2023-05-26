package com.arkeglorus.dreamsight

import android.telephony.TelephonyManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import java.util.*
import android.telephony.PhoneStateListener
import com.arkeglorus.dreamsight.v1.NotificationListener


class CallListener : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras!!.getString("android.intent.extra.PHONE_NUMBER")
        } else {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephony.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String) {
                    onCustomCallStateChanged(context, state, phoneNumber)
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    //Derived classes should override these to respond to specific events of interest
    private fun onIncomingCallStarted(ctx: Context, number: String?) {
        val intent = Intent("com.arkeglorus.dreamsight")
        intent.putExtra(NotificationListener.APPS_NAME, NotificationListener.ApplicationPackageNames.CALL_PACK_NAME)
        intent.putExtra(NotificationListener.CALL_NUMBER, number)
        ctx.sendBroadcast(intent)
    }

    private fun onOutgoingCallStarted(ctx: Context, number: String?) {
        val intent = Intent("com.arkeglorus.dreamsight")
        intent.putExtra(NotificationListener.APPS_NAME, NotificationListener.ApplicationPackageNames.CALL_PACK_NAME)
        intent.putExtra(NotificationListener.CALL_NUMBER, number)
        intent.putExtra(NotificationListener.CALL_ONGOING, true)
        ctx.sendBroadcast(intent)
    }

    private fun onIncomingCallEnded(ctx: Context, number: String?) {
        val intent = Intent("com.arkeglorus.dreamsight")
        intent.putExtra(NotificationListener.APPS_NAME, NotificationListener.ApplicationPackageNames.CALL_PACK_NAME)
        intent.putExtra(NotificationListener.CALL_NUMBER, number)
        intent.putExtra(NotificationListener.CALL_END, true)
        ctx.sendBroadcast(intent)
    }

    private fun onOutgoingCallEnded(ctx: Context, number: String?) {}

    private fun onMissedCall(ctx: Context, number: String?) {}

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private fun onCustomCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallStarted(context, number)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, savedNumber)
                }
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber)
                } else {
                    onOutgoingCallEnded(context, savedNumber)
                }
        }
        lastState = state
    }

    companion object {

        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null  //because the passed incoming is only valid in ringing
    }
}