package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.fsd.R

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // מוצאים את TextView של Register
        val registerText: TextView = findViewById(R.id.register_link_text)

        // מאזין ללחיצה על כל השורה
        registerText.setOnClickListener {
            // Intent שמעביר ל-RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
