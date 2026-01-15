package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val forgotPassword = findViewById<TextView>(R.id.forgot_password_link_text)

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, Forgot_PasswordActivity::class.java))
        }

    }
}
