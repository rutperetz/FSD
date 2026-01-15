package com.example.smart_group

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.network.ApiClient
import com.example.smart_group.network.ApiService
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ---- בדיקה שהאנדרואיד מצליח לדבר עם השרת (Render) ----
        val api = ApiClient.retrofit.create(ApiService::class.java)

        api.ping().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("SERVER", "Ping success. code=${response.code()} body=${response.body()}")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("SERVER", "Ping failed: ${t.message}", t)
            }
        })
        // ---------------------------------------------------------

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val signUpBtn = findViewById<MaterialButton>(R.id.signup_btn)

        backArrow.setOnClickListener {
            finish()
        }

        signUpBtn.setOnClickListener {

            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            val usernameRegex = Regex("^[A-Za-z]{1,15}$")
            val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,10}$")

            when {
                username.isEmpty() -> usernameInput.error = "Username is required"
                !usernameRegex.matches(username) ->
                    usernameInput.error = "Username must contain only English letters (max 15)"

                email.isEmpty() -> emailInput.error = "Email is required"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    emailInput.error = "Invalid email address"

                password.isEmpty() -> passwordInput.error = "Password is required"
                !passwordRegex.matches(password) ->
                    passwordInput.error = "Password must be 8–10 characters and include letters and numbers"

                else -> {
                    Toast.makeText(this, "Validation passed ✅", Toast.LENGTH_SHORT).show()
                    // כאן נחבר Register אמיתי לשרת כשיהיה לנו endpoint
                }
            }
        }
    }
}
