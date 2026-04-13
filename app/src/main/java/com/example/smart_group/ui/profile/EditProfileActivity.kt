package com.example.smart_group.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smart_group.R
import com.example.smart_group.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton
import com.example.smart_group.ui.home.HomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.smart_group.ui.questionnaire.EditQuestionnaireActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var vm: EditProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        vm = ViewModelProvider(this)[EditProfileViewModel::class.java]

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnSave = findViewById<MaterialButton>(R.id.save_changes_btn)
        val tvLogout = findViewById<TextView>(R.id.logout_text)

        vm.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                vm.onToastShown()
            }
        }

        vm.user.observe(this) { user ->
            user?.let {
                etUsername.setText(it.userName)
                etEmail.setText(it.email)
                etPassword.setText("")
            }
        }

        vm.state.observe(this) { state ->
            when (state) {
                is EditProfileState.Loading -> {
                    btnSave.isEnabled = false
                }

                is EditProfileState.Idle -> {
                    btnSave.isEnabled = true
                }

                is EditProfileState.NavigateBackToProfile -> {
                    btnSave.isEnabled = true
                    finish()
                }

                is EditProfileState.NavigateToLogin -> {
                    btnSave.isEnabled = true

                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    vm.logout()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        backArrow.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            vm.saveChanges(
                newUserNameRaw = etUsername.text.toString(),
                newEmailRaw = etEmail.text.toString(),
                newPasswordRaw = etPassword.text.toString()
            )
        }

        tvLogout.setOnClickListener {
            vm.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_search -> {
                    startActivity(Intent(this, com.example.smart_group.ui.search.SearchActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
        vm.loadUser()
    }
}