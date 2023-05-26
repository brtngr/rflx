package com.arkeglorus.dreamsight.model

import android.content.Intent
import android.graphics.drawable.Icon
import android.view.View
import com.arkeglorus.dreamsight.v1.NotificationListener

class Direction(view: View): Base(view) {
    var mapsNextDistance: String  = "-"
    var mapsDistance: String  = "-"
    var mapsDestination: String  = "-"
    var mapsDestinationEta: String  = "-"
    var mapsNextMove: String  = "-"
    var mapsSpeed: Float = 0f
    var mapsIcon: Icon? = null

    override fun setFrom(intent: Intent) {
        mapsNextDistance = intent.getStringExtra(NotificationListener.MAPS_NEXT_DISTANCE) ?: "-"
        mapsNextMove = intent.getStringExtra(NotificationListener.MAPS_NEXT_MOVE)?: "-"
        mapsDistance = intent.getStringExtra(NotificationListener.MAPS_DISTANCE) ?: "-"
        mapsDestination = intent.getStringExtra(NotificationListener.MAPS_DESTINATION) ?: "-"
        mapsDestinationEta = intent.getStringExtra(NotificationListener.MAPS_DESTINATION_ETA) ?: "-"
        mapsIcon = intent.extras?.get(NotificationListener.MAPS_ICON) as? Icon
    }

    override val itemIdentifier: String
        get() = identifier

    companion object {
        const val identifier: String = "DIRECTION"
    }
}