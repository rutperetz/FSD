package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
import android.widget.TextView
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth   // ✅ תוספת

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()     // ✅ תוספת

        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginBtn = findViewById<MaterialButton>(R.id.login_btn)

        // אם יש לך לינקים (לא חובה)
        // val registerText = findViewById<TextView>(R.id.register_text)
        // val forgotPasswordText = findViewById<TextView>(R.id.forgot_password_text)

        loginBtn.setOnClickListener {
            // ניקוי שגיאות קודמות
            emailInput.error = null
            passwordInput.error = null

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,10}$")

            when {
                email.isEmpty() -> {
                    emailInput.error = "Email is required"
                    emailInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Invalid email address"
                    emailInput.requestFocus()
                }

                password.isEmpty() -> {
                    passwordInput.error = "Password is required"
                    passwordInput.requestFocus()
                }

                !passwordRegex.matches(password) -> {
                    passwordInput.error =
                        "Password must be 8–10 characters and include letters and numbers"
                    passwordInput.requestFocus()
                }

                else -> {
                    // ✅ כאן במקום Toast "Login successful" — עושים Login אמיתי מול Firebase Auth
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Login successful ✅", Toast.LENGTH_SHORT).show()

                            // כשתהיה לך פעילות בית/מסך ראשי:
                            // startActivity(Intent(this, HomeActivity::class.java))
                            // finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Login failed: email or password incorrect",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }
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
}}
