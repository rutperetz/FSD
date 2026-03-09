package com.example.smart_group.ui.courses

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.smart_group.R

class AdminActivity : ComponentActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var spCategory: Spinner
    private lateinit var etVideoUrl: EditText
    private lateinit var spImage: Spinner
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        spCategory = findViewById(R.id.spCategory)
        etVideoUrl = findViewById(R.id.etVideoUrl)
        spImage = findViewById(R.id.spImage)
        btnSave = findViewById(R.id.btnSave)

        val categories = listOf("מתמטיקה", "שפות תכנות", "מדעי המחשב")
        spCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val images = listOf("img_math", "img_code", "img_cs")
        spImage.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, images)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val videoUrl = etVideoUrl.text.toString().trim()
            val category = spCategory.selectedItem.toString()
            val imageName = spImage.selectedItem.toString()

            if (title.isEmpty() || description.isEmpty() || videoUrl.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Course saved:\n$title\n$category\n$imageName",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}