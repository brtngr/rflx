package com.arkeglorus.dreamsight.model

import android.view.View

class Speedometer(view: View) : Base(view) {
    var speedMeterPerSecond: Float = 0f
        set(value) {
            val total = numberOfUpdate * speedAverageMeterPerSecond + value
            numberOfUpdate++
            speedAverageMeterPerSecond = total / numberOfUpdate
            field = value
        }
    var speedAverageMeterPerSecond: Float = 0f
    private var numberOfUpdate: Long = 0

    companion object {
        const val identifier: String = "SPEEDOMETER"
    }

    override val itemIdentifier: String
        get() = identifier
}