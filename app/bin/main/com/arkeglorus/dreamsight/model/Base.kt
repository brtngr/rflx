package com.arkeglorus.dreamsight.model

import android.content.Intent
import android.view.View

abstract class Base(var view: View) {

    open fun setFrom(intent: Intent) {}
    abstract val itemIdentifier: String
}