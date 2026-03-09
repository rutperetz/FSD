package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.smart_group.ui.group.GroupActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // זמנית – לפתוח את מסך הקבוצה ישר
        val intent = Intent(this, GroupActivity::class.java)
        intent.putExtra("courseId", "C1")
        intent.putExtra("studentId", "S1")
        startActivity(intent)

        finish()
    }
}