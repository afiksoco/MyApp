package com.example.myapp.Conditions

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import com.example.myapp.Condition
import com.example.myapp.R

class Brightness : Condition {
    override val name = "Brightness"
    override var isMet: Boolean = false
        private set

    private lateinit var dot: View
    private var onStateChanged: (() -> Unit)? = null
    private var context: Context? = null

    override fun attach(dotView: View, onStateChanged: () -> Unit) {
        this.dot = dotView
        this.onStateChanged = onStateChanged
    }

    override fun start(context: Context) {
        this.context = context.applicationContext

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val brightness = checkBrightnessNow()
                updateDot(brightness)
            }
        }

        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            false,
            observer
        )

        val currentStatus = checkBrightnessNow()
        updateDot(currentStatus)
    }


    private fun checkBrightnessNow(): Boolean {
        return try {
            val value = Settings.System.getInt(
                context?.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            value > 127
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun updateDot(metNow: Boolean) {
        if (isMet != metNow) {
            isMet = metNow
            dot.setBackgroundResource(if (isMet) R.drawable.dot_green else R.drawable.dot_red)
            onStateChanged?.invoke()
        }
    }
}
