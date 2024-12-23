package com.moehoemar.storyapp.views.story.ui.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences
import com.moehoemar.storyapp.data.preferences.dataStore
import com.moehoemar.storyapp.databinding.ActivityCreateStoryBinding
import com.moehoemar.storyapp.utils.createCustomTempFile
import com.moehoemar.storyapp.utils.displayToast
import com.moehoemar.storyapp.utils.getRotatedBitmap
import com.moehoemar.storyapp.utils.reduceFileImage
import com.moehoemar.storyapp.utils.uriToFile
import com.moehoemar.storyapp.views.story.StoryActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class CreateStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateStoryBinding
    private lateinit var preferences: StoryAppPreferences
    private lateinit var viewModel: CreateStoryViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val CAMERA_PERMISSION_CODE = 10

    private var cameraMode: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private lateinit var viewfinderContainer: ConstraintLayout
    private lateinit var cameraPreview: PreviewView
    private lateinit var shutterButton: ImageButton
    private lateinit var switchCameraButton: ImageButton
    private lateinit var selectFromGalleryButton: Button

    private lateinit var storyEditorContainer: ConstraintLayout
    private lateinit var selectedImage: ImageView
    private lateinit var storyDescription: EditText
    private lateinit var publishButton: ImageButton
    private lateinit var locationCheckBox: CheckBox

    private lateinit var loadingIndicator: ProgressBar

    private var selectedImageUri: Uri? = null
    private var isFrontFacing: Boolean = false
    private lateinit var imageCaptureUseCase: ImageCapture
    private var currentLocation: Location? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getLastLocation()
            }
            else -> {
                displayToast(this, getString(R.string.location_permission_denied))
                locationCheckBox.isChecked = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = StoryAppPreferences.getInstance(dataStore)
        viewModel = ViewModelProvider(
            this,
            CreateStoryViewModelFactory(this)
        )[CreateStoryViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initializeViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                displayToast(this, it)
                viewModel.resetError()
            }
        }

        viewModel.createStoryResponse.observe(this) { response ->
            if (response.error == false) {
                displayToast(this, getString(R.string.story_uploaded_successfully))
                onUploadSuccess()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (storyEditorContainer.visibility == View.VISIBLE) {
            storyEditorContainer.visibility = View.GONE
            viewfinderContainer.visibility = View.VISIBLE
            selectedImageUri = null
            initializeCamera()
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initializeViews() {
        with(binding) {
            viewfinderContainer = cameraxContainer
            cameraPreview = previewView
            switchCameraButton = buttonFlipCamera
            shutterButton = buttonCapture
            selectFromGalleryButton = buttonGallery

            storyEditorContainer = addStoryContainer
            selectedImage = ivImagePreview
            storyDescription = edAddDescription
            publishButton = buttonAdd
            locationCheckBox = cbLocation

            loadingIndicator = progressBar
        }

        checkAndRequestPermissions()
        setupButtonListeners()
        setupLocationCheckbox()
    }

    private fun setupLocationCheckbox() {
        locationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermissions()
            } else {
                currentLocation = null
            }
        }
    }

    private fun checkLocationPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                if (location != null) {
                    displayToast(this, getString(R.string.location_acquired))
                } else {
                    displayToast(this, getString(R.string.location_not_found))
                    locationCheckBox.isChecked = false
                }
            }
        } catch (e: SecurityException) {
            displayToast(this, getString(R.string.location_permission_denied))
            locationCheckBox.isChecked = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupButtonListeners() {
        switchCameraButton.setOnClickListener { toggleCamera() }
        shutterButton.setOnClickListener { captureImage() }
        selectFromGalleryButton.setOnClickListener { launchGallery() }
        publishButton.setOnClickListener { publishStory() }
    }

    private fun checkAndRequestPermissions() {
        if (hasRequiredPermissions()) {
            initializeCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                baseContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && hasRequiredPermissions()) {
            initializeCamera()
        }
    }

    private fun initializeCamera() {
        if (viewfinderContainer.visibility != View.VISIBLE) return

        applyCameraScale()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            setupCameraProvider(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }

    private fun applyCameraScale() {
        cameraPreview.scaleX = if (cameraMode == CameraSelector.DEFAULT_FRONT_CAMERA) -1f else 1f
    }

    private fun setupCameraProvider(cameraProvider: ProcessCameraProvider) {
        imageCaptureUseCase = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = cameraPreview.surfaceProvider
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraMode,
                preview,
                imageCaptureUseCase
            )
        } catch (exc: Exception) {
            displayToast(this, getString(R.string.camera_start_failed))
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun publishStory() {
        try {
            if (selectedImageUri == null) {
                displayToast(this, getString(R.string.please_choose_a_picture_first))
                return
            }

            val description = storyDescription.text.toString()
            if (description.isEmpty()) {
                displayToast(this, getString(R.string.please_add_description))
                return
            }

            loadingIndicator.visibility = View.VISIBLE

            val processedImage = uriToFile(selectedImageUri!!, this).reduceFileImage()
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val imageBody = processedImage.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("photo", processedImage.name, imageBody)

            uploadToServer(descriptionBody, imagePart)
        } catch (e: Exception) {
            loadingIndicator.visibility = View.GONE
            displayToast(this, "Error: ${e.message}")
        }
    }

    private fun uploadToServer(description: RequestBody, image: MultipartBody.Part) {
        val lat = if (locationCheckBox.isChecked && currentLocation != null) {
            currentLocation?.latitude.toString()
        } else "0"
        val long = if (locationCheckBox.isChecked && currentLocation != null) {
            currentLocation?.longitude.toString()
        } else "0"

        viewModel.createNewStory(
            description = description,
            photo = image,
            lat = lat.toRequestBody("text/plain".toMediaTypeOrNull()),
            long = long.toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }

    private fun onUploadSuccess() {
        val intent = Intent(this, StoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun toggleCamera() {
        isFrontFacing = !isFrontFacing
        cameraMode = if (isFrontFacing) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        initializeCamera()
    }

    private fun captureImage() {
        if (!::imageCaptureUseCase.isInitialized) {
            return displayToast(this, getString(R.string.camera_is_not_ready))
        }

        val photoFile = createCustomTempFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCaptureUseCase.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    processAndDisplayCapturedImage(output, photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    displayToast(
                        this@CreateStoryActivity,
                        getString(R.string.capture_failed, exc.message)
                    )
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun processAndDisplayCapturedImage(
        output: ImageCapture.OutputFileResults,
        photoFile: File,
    ) {
        try {
            selectedImageUri = output.savedUri ?: Uri.fromFile(photoFile)
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val rotatedBitmap = bitmap.getRotatedBitmap(photoFile)

            val processedFile = createCustomTempFile(application)
            FileOutputStream(processedFile).use { outputStream ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            runOnUiThread {
                selectedImage.setImageURI(Uri.fromFile(processedFile))
                viewfinderContainer.visibility = View.GONE
                storyEditorContainer.visibility = View.VISIBLE
            }

            selectedImageUri = Uri.fromFile(processedFile)
        } catch (e: Exception) {
            runOnUiThread {
                displayToast(this, "Error: ${e.message}")
            }
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    selectedImageUri = uri
                    runOnUiThread {
                        selectedImage.setImageURI(uri)
                        viewfinderContainer.visibility = View.GONE
                        storyEditorContainer.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    displayToast(this, "Error loading image: ${e.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        initializeCamera()
    }
}