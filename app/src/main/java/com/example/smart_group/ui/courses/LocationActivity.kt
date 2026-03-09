package com.example.smart_group.ui.courses

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.smart_group.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class LocationActivity : ComponentActivity() {

    private lateinit var tvLocation: TextView

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                fetchLocation()
            } else {
                tvLocation.text = "אין הרשאת מיקום"
                Toast.makeText(this, "אין הרשאת מיקום", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        tvLocation = findViewById(R.id.tvLocation)
        val btnGetLocation = findViewById<Button>(R.id.btnGetLocation)
        val btnOpenMaps = findViewById<Button>(R.id.btnOpenMaps)

        val prefs = getSharedPreferences("gps_prefs", Context.MODE_PRIVATE)
        val savedAddress = prefs.getString("address", null)

        tvLocation.text = savedAddress?.let { "הכתובת האחרונה:\n$it" }
            ?: "לחצי על 'קבל מיקום' כדי לקבל כתובת"

        btnGetLocation.setOnClickListener {
            ensurePermissionAndFetch()
        }

        btnOpenMaps.setOnClickListener {
            val lat = prefs.getString("lat", null)
            val lng = prefs.getString("lng", null)

            if (lat.isNullOrEmpty() || lng.isNullOrEmpty()) {
                Toast.makeText(this, "אין מיקום שמור עדיין", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun ensurePermissionAndFetch() {
        val finePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarsePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (finePermission == PackageManager.PERMISSION_GRANTED ||
            coarsePermission == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        tvLocation.text = "מחפש כתובת..."

        val prefs = getSharedPreferences("gps_prefs", Context.MODE_PRIVATE)

        try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        tvLocation.text = "לא נמצא מיקום. ודאי שהמיקום בטלפון דלוק"
                        return@addOnSuccessListener
                    }

                    val lat = location.latitude
                    val lng = location.longitude

                    val address = try {
                        val geocoder = Geocoder(this, Locale("he"))
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        addresses?.firstOrNull()?.getAddressLine(0)
                    } catch (e: Exception) {
                        null
                    }

                    val displayAddress = address ?: "לא נמצאה כתובת מדויקת"

                    tvLocation.text = "הכתובת הנוכחית:\n$displayAddress"

                    prefs.edit()
                        .putString("lat", lat.toString())
                        .putString("lng", lng.toString())
                        .putString("address", displayAddress)
                        .apply()
                }
                .addOnFailureListener { exception ->
                    tvLocation.text = "שגיאה בקבלת מיקום: ${exception.message}"
                }
        } catch (e: SecurityException) {
            tvLocation.text = "אין הרשאת מיקום"
        }
    }
}