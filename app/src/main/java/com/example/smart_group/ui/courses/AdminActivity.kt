//package com.example.smart_group.ui.courses
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.ArrayAdapter
//import android.widget.EditText
//import android.widget.Spinner
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import com.example.smart_group.R
//import com.example.smart_group.ui.profile.ProfileActivity
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.android.material.button.MaterialButton
//
//class AdminActivity : ComponentActivity() {
//
//    private lateinit var etTitle: EditText
//    private lateinit var etDescription: EditText
//    private lateinit var spCategory: Spinner
//    private lateinit var etVideoUrl: EditText
//    private lateinit var spImage: Spinner
//    private lateinit var btnSave: MaterialButton
//    private lateinit var bottomNav: BottomNavigationView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_admin)
//
//        etTitle = findViewById(R.id.etTitle)
//        etDescription = findViewById(R.id.etDescription)
//        spCategory = findViewById(R.id.spCategory)
//        etVideoUrl = findViewById(R.id.etVideoUrl)
//        spImage = findViewById(R.id.spImage)
//        btnSave = findViewById(R.id.btnSave)
//        bottomNav = findViewById(R.id.bottom_nav)
//
//        val categories = listOf("מתמטיקה", "שפות תכנות", "מדעי המחשב")
//        spCategory.adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_dropdown_item,
//            categories
//        )
//
//        val images = listOf("img_math", "img_code", "img_cs")
//        spImage.adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_dropdown_item,
//            images
//        )
//
//        btnSave.setOnClickListener {
//            val title = etTitle.text.toString().trim()
//            val description = etDescription.text.toString().trim()
//            val videoUrl = etVideoUrl.text.toString().trim()
//
//            if (title.isEmpty() || description.isEmpty() || videoUrl.isEmpty()) {
//                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Course saved successfully", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
//            finish()
//        }
//
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    startActivity(Intent(this, CoursesActivity::class.java))
//                    true
//                }
//                R.id.nav_search -> {
//                    Toast.makeText(this, "Search is available on courses page", Toast.LENGTH_SHORT).show()
//                    true
//                }
//                R.id.nav_profile -> {
//                    startActivity(Intent(this, ProfileActivity::class.java))
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//}