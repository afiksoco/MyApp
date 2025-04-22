package com.example.myapp

import android.content.Context
import android.view.View

interface Condition {
    val name: String
    val isMet: Boolean
    fun start(context: Context)
    fun attach(dotView: View, onStateChanged: () -> Unit)
}
