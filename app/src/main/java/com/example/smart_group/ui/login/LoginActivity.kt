package com.example.smart_group.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.smart_group.R
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.ui.login.LoginViewModel
import com.example.smart_group.ui.register.RegisterActivity
import com.example.smart_group.Forgot_PasswordActivity
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

        //לחיצה כל כפתור הLOGIN
        loginBtn.setOnClickListener {
            viewModel.login(
                emailRaw = emailInput.text.toString(),
                passwordRaw = passwordInput.text.toString()
            )
        }

        //לחיצה על המילה REGISTER
        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //לחיצה על שכחתי סיסמא
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, Forgot_PasswordActivity::class.java))
        }

        //מאזין לתוצאות שבאו מהVIEWMODLE
        //Toast messages
        viewModel.toastMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.onToastShown()
            }
        }

        //Loading-disable button
        viewModel.isLoading.observe(this) { loading ->
            loginBtn.isEnabled = !loading
        }

        //Navigation on success
        viewModel.navigateToNext.observe(this) { go ->
            if (go) {
                Toast.makeText(this, "Login successful ✅", Toast.LENGTH_SHORT).show()

                // כאן בעתיד תעברי למסך הבא (כשיהיה לך)
                // startActivity(Intent(this, HomeActivity::class.java))
                // finish()

                viewModel.onNavigated()
            }
        }
    }
}