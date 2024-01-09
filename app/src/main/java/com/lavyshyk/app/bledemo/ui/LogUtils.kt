package com.lavyshyk.app.bledemo.ui

import android.util.Log

fun <T> T.log(prefix: String = ""): T {
    Log.d("lavip", "$prefix ${this.toString()}")
    return this
}