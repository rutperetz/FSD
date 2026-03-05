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
            vm.saveChanges(
                newUserName = etUsername.text.toString().trim(),
                newEmail = etEmail.text.toString().trim(),
                newPassword = etPassword.text.toString() // אם ריק -> לא יעדכן
            )
        }

        tvLogout.setOnClickListener {
            vm.logout()
            // כרגע פשוט חוזרים אחורה, אחרי זה נחבר למסך login
            finishAffinity()
        }

        vm.loadUser()
    }
}