package com.example.myapp.Conditions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import com.example.myapp.Condition
import com.example.myapp.R

class PhotoCaptureCondition : Condition {

    override val name: String = "PhotoCapture"
    override var isMet: Boolean = false
        private set

    private lateinit var dot: View
    private var onStateChanged: (() -> Unit)? = null
    private lateinit var context: Context

    private var photoLauncher: ActivityResultLauncher<Intent>? = null

    override fun attach(dotView: View, onStateChanged: () -> Unit) {
        this.dot = dotView
        this.onStateChanged = onStateChanged
        updateDot()
    }

    override fun start(context: Context) {
        this.context = context
        // We trigger the photo request externally from MainActivity for now
    }

    fun registerLauncher(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        this.photoLauncher = launcher
        // Optionally trigger here, or manually from MainActivity
    }

    fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoLauncher?.launch(intent)
    }

    fun onPhotoCaptured(success: Boolean) {
        if (success) {
            isMet = true
            updateDot()
            onStateChanged?.invoke()
        }
    }

    private fun updateDot() {
        if (::dot.isInitialized) {
            dot.setBackgroundResource(if (isMet) R.drawable.dot_green else R.drawable.dot_red)
        }
    }
}
