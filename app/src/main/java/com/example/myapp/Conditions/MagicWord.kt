package com.example.myapp.Conditions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.myapp.Condition
import com.example.myapp.R

class MagicWordCondition(private val inputField: EditText) : Condition {

    override val name: String = "MagicWord"
    override var isMet: Boolean = false
        private set

    private lateinit var dot: View
    private var onStateChanged: (() -> Unit)? = null

    private var ssid: String = ""
    private var batteryPercent: Int = -1
    private lateinit var context: Context

    override fun attach(dotView: View, onStateChanged: () -> Unit) {
        this.dot = dotView
        this.onStateChanged = onStateChanged
        updateDot()
    }

    override fun start(context: Context) {
        this.context = context

        if (!isWifiConnected(context)) {
            Toast.makeText(context, "Please connect to Wi-Fi first!", Toast.LENGTH_LONG).show()
            ssid = ""
        } else {
            ssid = getCurrentSsid(context)
        }

        batteryPercent = getBatteryLevel(context)

        inputField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validate()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validate() {
        if (ssid.isBlank()) {
            // Can't validate without Wi-Fi
            return
        }
        val expectedMagicWord = "$ssid$batteryPercent"
        val userInput = inputField.text.toString().trim()

        val nowMet = (userInput == expectedMagicWord)
        if (isMet != nowMet) {
            isMet = nowMet
            updateDot()
            onStateChanged?.invoke()
        }
    }

    private fun updateDot() {
        if (::dot.isInitialized) {
            dot.setBackgroundResource(if (isMet) R.drawable.dot_green else R.drawable.dot_red)
        }
    }

    private fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    private fun getCurrentSsid(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return info.ssid.removePrefix("\"").removeSuffix("\"")
    }

    private fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else -1
    }
}
