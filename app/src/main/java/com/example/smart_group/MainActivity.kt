package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.smart_group.ui.courses.CoursesActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, CoursesActivity::class.java))
        finish()
    }
}