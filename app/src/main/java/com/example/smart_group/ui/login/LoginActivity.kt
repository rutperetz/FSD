package com.example.smart_group.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.R
import com.example.smart_group.RegisterActivity
import com.example.smart_group.ui.courses.AdminActivity
import com.example.smart_group.ui.forgotpassword.ForgotPasswordActivity
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginBtn = findViewById<MaterialButton>(R.id.login_btn)
        val registerText = findViewById<TextView>(R.id.register_link_text)
        val forgotPasswordText = findViewById<TextView>(R.id.forgot_password_link_text)

        loginBtn.setOnClickListener {
            viewModel.login(
                emailRaw = emailInput.text.toString(),
                passwordRaw = passwordInput.text.toString()
            )
        }

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.onToastShown()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            loginBtn.isEnabled = !loading
        }

        viewModel.navigateToStudent.observe(this) { go ->
            if (go) {
                Toast.makeText(this, "Student login successful ✅", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
                viewModel.onStudentNavigated()
            }
        }

        viewModel.navigateToAdmin.observe(this) { go ->
            if (go) {
                Toast.makeText(this, "Admin login successful ✅", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
                viewModel.onAdminNavigated()
            }
        }
    }
}