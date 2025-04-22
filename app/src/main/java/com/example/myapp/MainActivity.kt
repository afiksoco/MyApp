package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.Conditions.Brightness
import com.example.myapp.Conditions.PhotoCaptureCondition
import com.example.myapp.Conditions.VolumePatternCondition
import com.example.myapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val conditions = mutableListOf<Condition>()

    private val volumeCondition = VolumePatternCondition()
    private lateinit var brightnessCondition: Brightness
    private lateinit var photoCondition: PhotoCaptureCondition

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Label setup
        binding.rowMagicWord.conditionLabel.text = Constants.MAGIC_WORD
        binding.rowPhoto.conditionLabel.text = Constants.PHOTO
        binding.rowProximity.conditionLabel.text = Constants.PROXIMITY
        binding.rowVolume.conditionLabel.text = Constants.VOLUME
        binding.rowBrightness.conditionLabel.text = Constants.BRIGHTNESS

        // === Setup CAMERA PERMISSION REQUEST first ===
        requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                photoCondition.launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        // === Setup CAMERA RESULT LAUNCHER ===
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val success = result.resultCode == Activity.RESULT_OK
            photoCondition.onPhotoCaptured(success)
        }

        // === Photo Condition ===
        photoCondition = PhotoCaptureCondition()
        photoCondition.attach(binding.rowPhoto.conditionDot) { checkAllConditions() }
        photoCondition.start(this)
        photoCondition.registerLauncher(this, cameraLauncher)
        conditions.add(photoCondition)

        // === Ask for permission (will launch camera if granted) ===
        binding.rowPhoto.root.setOnClickListener {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }

        // === Brightness Condition ===
        brightnessCondition = Brightness()
        brightnessCondition.attach(binding.rowBrightness.conditionDot) { checkAllConditions() }
        brightnessCondition.start(this)
        conditions.add(brightnessCondition)

        // === Volume Condition ===
        volumeCondition.attach(binding.rowVolume.conditionDot) { checkAllConditions() }
        volumeCondition.start(this)
        conditions.add(volumeCondition)
    }

    private fun checkAllConditions() {
        val allMet = conditions.all { it.isMet }
        binding.loginButton.isEnabled = allMet
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (volumeCondition.onKeyPressed(event.keyCode)) {
                return true // consume it
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
