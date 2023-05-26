package com.arkeglorus.dreamsight.model

import android.view.View
import java.util.*

class ClockWeather(view: View): Base(view) {
    override val itemIdentifier: String
        get() = identifier
    var currentTime: Date
        get() {
            return Date()
        }
        set(value) {}
    var weatherUrl: String = ""

    companion object {
        const val identifier: String = "CLOCKWEATHER"
    }
}