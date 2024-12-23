package com.moehoemar.storyapp.views.maps

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.remote.response.story.ListStoryItem
import com.moehoemar.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsViewModel
    private lateinit var boundsBuilder: LatLngBounds.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupViewModel()
        observeViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            MapsViewModelFactory(this)
        )[MapsViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.stories.observe(this) { stories ->
            stories?.let {
                addManyMarker(it)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        viewModel.getStoriestWithLocation()
    }


    private fun addManyMarker(stories: List<ListStoryItem>) {
        boundsBuilder = LatLngBounds.Builder()
        var hasValidLocation = false

        stories.forEach { story ->
            if (story.lat != null && story.lon != null) {
                hasValidLocation = true
                val latLng = LatLng(story.lat, story.lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(story.name)
                        .snippet(story.description)
                )
                boundsBuilder.include(latLng)
            }
        }

        if (hasValidLocation) {
            val bounds = boundsBuilder.build()
            mMap.setOnMapLoadedCallback {
                try {
                    val padding = 100
                    val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    mMap.animateCamera(cu)
                } catch (e: Exception) {
                    try {
                        val width = resources.displayMetrics.widthPixels
                        val height = resources.displayMetrics.heightPixels
                        val padding = (Math.min(width, height) * 0.15).toInt()
                        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
                        mMap.animateCamera(cu)
                    } catch (e: Exception) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(bounds.center))
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}