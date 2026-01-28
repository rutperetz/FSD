package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.ui.group.CourseGroupActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, CourseGroupActivity::class.java).apply {
            putExtra("courseId", "C1")
            putExtra("roundId", "C1-R1")
            putExtra("groupId", "C1-R1-G1")
        }
        startActivity(intent)

        finish() // optional
    }
}
