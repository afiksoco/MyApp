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
import com.example.myapp.Conditions.*
import com.example.myapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val conditions = mutableListOf<Condition>()

    private lateinit var volumeCondition: VolumePatternCondition
    private lateinit var brightnessCondition: Brightness
    private lateinit var photoCondition: PhotoCaptureCondition
    private lateinit var proximityCondition: ProximityCondition
    private lateinit var magicWordCondition: MagicWordCondition

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>
    private lateinit var requestLocationPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === Setup Labels ===
        binding.rowMagicWord.conditionLabel.text = Constants.MAGIC_WORD
        binding.rowPhoto.conditionLabel.text = Constants.PHOTO
        binding.rowProximity.conditionLabel.text = Constants.PROXIMITY
        binding.rowVolume.conditionLabel.text = Constants.VOLUME
        binding.rowBrightness.conditionLabel.text = Constants.BRIGHTNESS
        binding.hintButton.setOnClickListener{
            Toast.makeText(this, "Magic Word is wi-fi name + battery %", Toast.LENGTH_SHORT).show()

        }
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, SuccessActivity::class.java)
            startActivity(intent)
        }

        // === Setup CAMERA Permission Request ===
        requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                photoCondition.launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        // === Setup LOCATION Permission Request ===
        requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                magicWordCondition.start(this)
            } else {
                Toast.makeText(this, "Location permission is required for Wi-Fi access.", Toast.LENGTH_LONG).show()
            }
        }

        // === Setup Camera ActivityResult Launcher ===
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val success = result.resultCode == Activity.RESULT_OK
            photoCondition.onPhotoCaptured(success)
        }

        // === Setup Conditions ===

        // 1. Photo
        photoCondition = PhotoCaptureCondition()
        photoCondition.attach(binding.rowPhoto.conditionDot) { checkAllConditions() }
        photoCondition.start(this)
        photoCondition.registerLauncher(this, cameraLauncher)
        binding.rowPhoto.root.setOnClickListener {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }
        conditions.add(photoCondition)

        // 2. Brightness
        brightnessCondition = Brightness()
        brightnessCondition.attach(binding.rowBrightness.conditionDot) { checkAllConditions() }
        brightnessCondition.start(this)
        conditions.add(brightnessCondition)

        // 3. Volume Pattern
        volumeCondition = VolumePatternCondition()
        volumeCondition.attach(binding.rowVolume.conditionDot) { checkAllConditions() }
        volumeCondition.start(this)
        conditions.add(volumeCondition)

        // 4. Proximity
        proximityCondition = ProximityCondition()
        proximityCondition.attach(binding.rowProximity.conditionDot) { checkAllConditions() }
        proximityCondition.start(this)
        conditions.add(proximityCondition)

        // 5. Magic Word (Wi-Fi SSID + Battery %)
        magicWordCondition = MagicWordCondition(binding.magicInput)
        magicWordCondition.attach(binding.rowMagicWord.conditionDot) { checkAllConditions() }
        conditions.add(magicWordCondition)

        // Request Location permission BEFORE starting Magic Word condition
        requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
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
