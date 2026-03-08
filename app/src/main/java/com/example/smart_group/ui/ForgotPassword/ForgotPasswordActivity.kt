package com.example.smart_group.ui.forgotpassword

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.R
import com.example.smart_group.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton

class ForgotPasswordActivity : AppCompatActivity() {

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val emailInput = findViewById<EditText>(R.id.email_input_forgot)
        val sendResetBtn = findViewById<MaterialButton>(R.id.send_code_btn)

        backArrow.setOnClickListener {
            finish()
        }

        sendResetBtn.setOnClickListener {
            viewModel.sendResetEmail(emailInput.text.toString())
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.onToastShown()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            sendResetBtn.isEnabled = !loading
        }

        viewModel.navigateBackToLogin.observe(this) { go ->
            if (go) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                viewModel.onNavigated()
            }
        }
    }
}