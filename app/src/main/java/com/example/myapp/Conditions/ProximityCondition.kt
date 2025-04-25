package com.example.myapp.Conditions

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.example.myapp.Condition
import com.example.myapp.R

class ProximityCondition : Condition {

    override val name: String = "Proximity"
    override var isMet: Boolean = false
        private set

    private lateinit var dot: View
    private var onStateChanged: (() -> Unit)? = null
    private var coveredAt: Long? = null
    private val requiredHoldTime = 3000L // milliseconds
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private val handler = Handler(Looper.getMainLooper())
    private var context: Context? = null
    private var toast: Toast? = null


    override fun attach(dotView: View, onStateChanged: () -> Unit) {
        this.dot = dotView
        this.onStateChanged = onStateChanged
        updateDot()
    }

    override fun start(context: Context) {
        this.context = context
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        proximitySensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val isCovered = event.values[0] == 0f

            if (isMet) return

            if (isCovered && coveredAt == null) {
                coveredAt = System.currentTimeMillis()

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        val elapsed = System.currentTimeMillis() - (coveredAt ?: 0)
                        if (!isMet && coveredAt != null && elapsed >= requiredHoldTime) {
                            isMet = true
                            updateDot()
                            toast?.cancel()
                            Toast.makeText(context, "✅ Proximity unlocked!", Toast.LENGTH_SHORT).show()
                            onStateChanged?.invoke()
                        } else if (!isMet && coveredAt != null) {
                            showProgressToast(elapsed)
                            handler.postDelayed(this, 200)
                        }
                    }
                }, 0)
            }

            if (!isCovered) {
                coveredAt = null
                toast?.cancel()
            }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


    private fun showProgressToast(elapsed: Long) {
        toast?.cancel()
        toast = Toast.makeText(context, "⏳ Holding... ${elapsed}ms", Toast.LENGTH_SHORT)
        toast?.show()
    }


    private fun updateDot() {
        if (::dot.isInitialized) {
            dot.setBackgroundResource(if (isMet) R.drawable.dot_green else R.drawable.dot_red)
        }
    }
}
