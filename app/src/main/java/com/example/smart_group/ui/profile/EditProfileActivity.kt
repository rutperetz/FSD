package com.example.smart_group.ui.profile

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smart_group.R
import com.google.android.material.button.MaterialButton
import android.content.Intent
import com.example.smart_group.ui.login.LoginActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var vm: EditProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        vm = ViewModelProvider(this)[EditProfileViewModel::class.java]

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnSave = findViewById<MaterialButton>(R.id.save_changes_btn)
        val tvLogout = findViewById<TextView>(R.id.logout_text)

        vm.usernameError.observe(this) { msg ->
            etUsername.error = msg
        }

        vm.emailError.observe(this) { msg ->
            etEmail.error = msg
        }

        vm.passwordError.observe(this) { msg ->
            etPassword.error = msg
        }

        vm.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                vm.onToastShown()
            }
        }
        backArrow.setOnClickListener { finish() }

        vm.user.observe(this) { user ->
            if (user != null) {
                etUsername.setText(user.userName)
                etEmail.setText(user.email)
                etPassword.setText("") // לא מציגים סיסמה קיימת
            }
        }


        vm.state.observe(this) { state ->
            when (state) {
                is EditProfileState.Loading -> {
                    // אפשר להוסיף disable לכפתור זמנית
                    btnSave.isEnabled = false
                }
                is EditProfileState.Saved -> {
                    btnSave.isEnabled = true
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()

                    val i = Intent(this, ProfileActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(i)
                    finish()
                }

                is EditProfileState.Error -> {
                    btnSave.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    btnSave.isEnabled = true
                }
            }
        }

        btnSave.setOnClickListener {
            etUsername.error = null
            etEmail.error = null
            etPassword.error = null

            vm.saveChanges(
                newUserNameRaw = etUsername.text.toString(),
                newEmailRaw = etEmail.text.toString(),
                newPasswordRaw = etPassword.text.toString()
            )
        }

        tvLogout.setOnClickListener {
            vm.logout()
            val i = Intent(this, LoginActivity::class.java) // לשים את השם המדויק אצלך
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        vm.loadUser()
    }
}