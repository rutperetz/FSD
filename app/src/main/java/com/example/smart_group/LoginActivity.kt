package com.example.smart_group

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fsd.R
import android.content.Intent
import android.widget.TextView


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val forgotPassword = findViewById<TextView>(R.id.forgot_password_link_text)
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, Forgot_PasswordActivity::class.java))
        }

    }
}
