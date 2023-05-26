package com.arkeglorus.dreamsight.model

import android.content.Intent
import android.view.View
import com.arkeglorus.dreamsight.v1.NotificationListener

class Whatsapp(view: View) : Base(view) {
    var waTitle: String  = "-"
    var waText: String  = "-"

    override fun setFrom(intent: Intent) {
        waTitle = intent.getStringExtra(NotificationListener.WA_TITLE)?: "-"
        waText = intent.getStringExtra(NotificationListener.WA_TEXT)?: "-"
    }

    override val itemIdentifier: String
        get() = identifier

    companion object {
        const val identifier: String = "WA"
    }

}