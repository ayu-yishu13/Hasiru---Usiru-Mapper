package com.hasiru.usiru.mapper.ui.addtree

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hasiru.usiru.mapper.HasiruApp
import com.hasiru.usiru.mapper.R
import com.hasiru.usiru.mapper.data.local.TreeEntity
import com.hasiru.usiru.mapper.ui.map.MapActivity
import com.hasiru.usiru.mapper.util.BitmapUtils
import com.hasiru.usiru.mapper.util.LocationHelper
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddTreeActivity : AppCompatActivity() {
    private val viewModel: AddTreeViewModel by viewModels {
        val app = application as HasiruApp
        AddTreeViewModel.Factory(app.repository, app.geminiTreeIdentifier)
    }
    private lateinit var previewView: PreviewView
    private lateinit var capturedImage: ImageView
    private lateinit var loadingLayout: View
    private lateinit var formLayout: View
    private lateinit var locationText: TextView
    private lateinit var speciesInput: TextInputEditText
    private lateinit var scientificInput: TextInputEditText
    private lateinit var kannadaInput: TextInputEditText
    private lateinit var girthLayout: TextInputLayout
    private lateinit var girthInput: TextInputEditText
    private lateinit var healthGroup: RadioGroup
    private lateinit var emptyPitSwitch: Switch
    private lateinit var nativeCheck: CheckBox
    private lateinit var oxygenScoreText: TextView
    private lateinit var latitudeInput: TextInputEditText
    private lateinit var longitudeInput: TextInputEditText
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var photoUri: Uri? = null
    private var latestBitmap: Bitmap? = null
    private var latitude = 12.9716
    private var longitude = 77.5946
    private var accuracy: Float? = null
    private var speciesFactor = 1f
    private val locationHelper by lazy { LocationHelper(this) }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.CAMERA] == true) startCamera() else showCameraDeniedDialog()
        if (locationHelper.hasLocationPermission()) loadLocation() else showLocationDeniedDialog()
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { handleGalleryImage(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tree)
        bindViews()
        requestPermissionsAndStart()
        observeViewModel()
        findViewById<MaterialButton>(R.id.captureButton).setOnClickListener { capturePhoto() }
        findViewById<MaterialButton>(R.id.galleryButton).setOnClickListener { galleryLauncher.launch("image/*") }
        findViewById<MaterialButton>(R.id.saveTreeButton).setOnClickListener { saveTree() }
        girthInput.addTextChangedListener { viewModel.updateGirth(it.toString().toFloatOrNull() ?: 0f) }
    }

    private fun bindViews() {
        previewView = findViewById(R.id.previewView)
        capturedImage = findViewById(R.id.capturedImageView)
        loadingLayout = findViewById(R.id.aiLoadingLayout)
        formLayout = findViewById(R.id.treeFormLayout)
        locationText = findViewById(R.id.locationAccuracyText)
        speciesInput = findViewById(R.id.speciesInput)
        scientificInput = findViewById(R.id.scientificInput)
        kannadaInput = findViewById(R.id.kannadaInput)
        girthLayout = findViewById(R.id.girthInputLayout)
        girthInput = findViewById(R.id.girthInput)
        healthGroup = findViewById(R.id.healthRadioGroup)
        emptyPitSwitch = findViewById(R.id.emptyPitSwitch)
        nativeCheck = findViewById(R.id.nativeCheckBox)
        oxygenScoreText = findViewById(R.id.oxygenScoreText)
        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)
        formLayout.visibility = View.GONE
        loadingLayout.visibility = View.GONE
    }

    private fun requestPermissionsAndStart() {
        val needed = listOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isEmpty()) { startCamera(); loadLocation() } else permissionLauncher.launch(needed.toTypedArray())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadLocation() = lifecycleScope.launch {
        val location = locationHelper.getCurrentLocation()
        if (location != null) {
            latitude = location.latitude; longitude = location.longitude; accuracy = location.accuracy
        }
        updateLocationInputs()
    }

    private fun updateLocationInputs() {
        latitudeInput.setText(latitude.toString())
        longitudeInput.setText(longitude.toString())
        locationText.text = if (accuracy != null) "Location accuracy: ±${accuracy!!.toInt()} m${if (accuracy!! <= 5f) " ✓" else " — move outdoors for ≤5 m"}" else "Manual location fallback enabled"
    }

    private fun capturePhoto() {
        val capture = imageCapture ?: return
        val file = BitmapUtils.createImageFile(this)
        capture.takePicture(BitmapUtils.outputOptions(file), ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) = handleCapturedFile(file)
            override fun onError(exception: ImageCaptureException) = Snackbar.make(previewView, exception.message ?: "Camera failed", Snackbar.LENGTH_LONG).show()
        })
    }

    private fun handleCapturedFile(file: File) {
        photoUri = Uri.fromFile(file)
        latestBitmap = BitmapUtils.decodeFile(file)
        capturedImage.load(file)
        capturedImage.visibility = View.VISIBLE
        latestBitmap?.let { viewModel.identify(it) }
    }

    private fun handleGalleryImage(uri: Uri) {
        photoUri = uri
        latestBitmap = BitmapUtils.decodeUri(contentResolver, uri)
        capturedImage.load(uri)
        capturedImage.visibility = View.VISIBLE
        latestBitmap?.let { viewModel.identify(it) }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.identificationState.collect { state ->
                when (state) {
                    IdentificationState.Idle -> Unit
                    IdentificationState.Loading -> { loadingLayout.visibility = View.VISIBLE; formLayout.visibility = View.GONE }
                    is IdentificationState.Success -> {
                        loadingLayout.visibility = View.GONE; formLayout.visibility = View.VISIBLE
                        speciesInput.setText(state.data.species); scientificInput.setText(state.data.scientificName); kannadaInput.setText(state.data.kannadaName)
                        speciesFactor = state.data.speciesFactor; nativeCheck.isChecked = state.data.isNative
                        viewModel.updateSpeciesFactor(speciesFactor)
                        when (state.data.health) { "Sick" -> healthGroup.check(R.id.healthSick); "Dead" -> healthGroup.check(R.id.healthDead); else -> healthGroup.check(R.id.healthGood) }
                    }
                    is IdentificationState.Error -> {
                        loadingLayout.visibility = View.GONE; formLayout.visibility = View.VISIBLE
                        Snackbar.make(formLayout, "Could not identify tree. Please fill details manually.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
        lifecycleScope.launch { viewModel.oxygenScore.collect { oxygenScoreText.text = "Oxygen Score: %.1f".format(it) } }
    }

    private fun saveTree() {
        val girth = girthInput.text.toString().toFloatOrNull()
        if (girth == null || girth <= 0f) { girthLayout.error = "Girth in cm is required"; return } else girthLayout.error = null
        latitude = latitudeInput.text.toString().toDoubleOrNull() ?: latitude
        longitude = longitudeInput.text.toString().toDoubleOrNull() ?: longitude
        val health = when (healthGroup.checkedRadioButtonId) { R.id.healthSick -> "Sick"; R.id.healthDead -> "Dead"; else -> "Good" }
        val tree = TreeEntity(
            latitude = latitude,
            longitude = longitude,
            species = speciesInput.text?.toString().orEmpty().ifBlank { if (emptyPitSwitch.isChecked) "Empty Pit" else "Unknown" },
            scientificName = scientificInput.text?.toString().orEmpty(),
            kannadaName = kannadaInput.text?.toString().orEmpty(),
            healthStatus = health,
            isNative = nativeCheck.isChecked,
            isEmptyPit = emptyPitSwitch.isChecked,
            girthCm = girth,
            speciesFactor = speciesFactor,
            oxygenScore = girth * speciesFactor,
            photoUri = photoUri?.toString(),
            taggedBy = android.os.Build.MODEL ?: "Citizen"
        )
        lifecycleScope.launch {
            viewModel.saveTree(tree)
            startActivity(Intent(this@AddTreeActivity, MapActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    private fun showLocationDeniedDialog() = AlertDialog.Builder(this).setTitle("Location permission").setMessage(R.string.location_needed).setPositiveButton("Enter manually", null).show()
    private fun showCameraDeniedDialog() = AlertDialog.Builder(this).setTitle("Camera permission").setMessage(R.string.camera_needed).setPositiveButton("Choose gallery") { _, _ -> galleryLauncher.launch("image/*") }.show()

    override fun onDestroy() { super.onDestroy(); cameraExecutor.shutdown() }
}
