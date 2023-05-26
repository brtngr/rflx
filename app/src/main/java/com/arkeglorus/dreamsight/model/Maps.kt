package com.arkeglorus.dreamsight.model

import android.content.Intent
import android.view.View
import com.arkeglorus.dreamsight.v1.NotificationListener

class Maps(view: View) : Base(view) {
    var mapsNextDistance: String  = "-"
    var mapsDistance: String  = "-"
    var mapsDestination: String  = "-"
    var mapsDestinationEta: String  = "-"
    var mapsSpeed: Float = 0f

    override fun setFrom(intent: Intent) {
        mapsNextDistance = intent.getStringExtra(NotificationListener.MAPS_NEXT_DISTANCE) ?: "-"
        mapsDistance = intent.getStringExtra(NotificationListener.MAPS_DISTANCE) ?: "-"
        mapsDestination = intent.getStringExtra(NotificationListener.MAPS_DESTINATION) ?: "-"
        mapsDestinationEta = intent.getStringExtra(NotificationListener.MAPS_DESTINATION_ETA) ?: "-"
    }

    override val itemIdentifier: String
        get() = identifier

    companion object {
        const val identifier: String = "MAPS"
    }

}