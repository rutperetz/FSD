package com.example.smart_group.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.smart_group.R
import com.example.smart_group.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton
import android.widget.ImageView


class RegisterActivity : ComponentActivity() {

    private val regVm: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val usernameBox = findViewById<EditText>(R.id.username_input)
        val emailBox = findViewById<EditText>(R.id.email_input)
        val passwordBox = findViewById<EditText>(R.id.password_input)
        val signUpBtn = findViewById<MaterialButton>(R.id.signup_btn)

        backArrow.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signUpBtn.setOnClickListener {
            regVm.signUp(
                usernameRaw = usernameBox.text.toString(),
                emailRaw = emailBox.text.toString(),
                passwordRaw = passwordBox.text.toString()
            )
        }

        regVm.usernameError.observe(this) { err -> usernameBox.error = err }
        regVm.emailError.observe(this) { err -> emailBox.error = err }
        regVm.passwordError.observe(this) { err -> passwordBox.error = err }

        regVm.toastMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                regVm.onToastShown()
            }
        }

        regVm.isLoading.observe(this) { loading ->
            signUpBtn.isEnabled = !loading
        }

        regVm.navigateToLogin.observe(this) { go ->
            if (go) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                regVm.onNavigatedToLogin()
            }
        }
    }
}
