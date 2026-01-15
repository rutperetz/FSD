package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerText: TextView = findViewById(R.id.register_link_text)

        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        val forgotPassword = findViewById<TextView>(R.id.forgot_password_link_text)

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, Forgot_PasswordActivity::class.java))
        }

    }
}
