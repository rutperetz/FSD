package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.smart_group.ui.group.CourseGroupActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val intent = Intent(this, CourseGroupActivity::class.java).apply {
            putExtra("courseId", "C1")
            putExtra("roundId", "R1")
            putExtra("groupId", "G1")
        }
        startActivity(intent)
        finish()
    }
}