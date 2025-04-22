package com.example.myapp.Conditions

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.example.myapp.Condition
import com.example.myapp.R

class VolumePatternCondition : Condition {
    override val name = "VolumePattern"
    override var isMet: Boolean = false
        private set
    private lateinit var context: Context
    private lateinit var dot: View
    private var onStateChanged: (() -> Unit)? = null

    // Store button presses: 1 = UP, -1 = DOWN
    private val inputSequence = mutableListOf<Int>()
    private val expectedPattern = listOf(1, 1, -1, -1, 1)

    override fun attach(dotView: View, onStateChanged: () -> Unit) {
        this.dot = dotView
        this.onStateChanged = onStateChanged
        updateDot()
    }

    override fun start(context: Context) {
        this.context = context.applicationContext
    }

    fun onKeyPressed(keyCode: Int): Boolean {
        if (isMet) return false

        val input = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> 1
            KeyEvent.KEYCODE_VOLUME_DOWN -> -1
            else -> return false
        }

        inputSequence.add(input)

        // Trim if longer than pattern
        if (inputSequence.size > expectedPattern.size) {
            inputSequence.removeAt(0)
        }

        // Check if sequence so far is correct prefix
        val correctSoFar = inputSequence == expectedPattern.take(inputSequence.size)

        if (correctSoFar) {
            if (inputSequence == expectedPattern) {
                isMet = true
                updateDot()
                showToast("🎉 Pattern complete!")
                onStateChanged?.invoke()
            } else {
                showToast("✅ ${inputSequence.size} correct...")
            }
        } else {
            showToast("❌ Nope! Start over.")
            inputSequence.clear()
        }

        return true
    }
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    private fun updateDot() {
        if (::dot.isInitialized) {
            dot.setBackgroundResource(if (isMet) R.drawable.dot_green else R.drawable.dot_red)
        }
    }
}
