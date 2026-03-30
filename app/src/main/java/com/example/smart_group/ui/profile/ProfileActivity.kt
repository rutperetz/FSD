package com.example.smart_group.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smart_group.R
import com.example.smart_group.ui.home.HomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var vm: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        vm = ViewModelProvider(this)[ProfileViewModel::class.java]

        val tvUserName = findViewById<TextView>(R.id.tv_user_name)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val btnEditProfile = findViewById<MaterialButton>(R.id.btn_edit_profile)
        val btnQuestionnaire = findViewById<MaterialButton>(R.id.btn_view_questionnaire)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnQuestionnaire.setOnClickListener {
            Toast.makeText(this, "Questionnaire screen not implemented yet", Toast.LENGTH_SHORT).show()
        }

        vm.state.observe(this) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    // optional loader
                }

                is ProfileState.Success -> {
                    tvUserName.text = state.user.userName
                    tvEmail.text = state.user.email
                }

                is ProfileState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
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
                    Toast.makeText(this, "Search screen not implemented yet", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_profile -> {
                    true
                }

                else -> false
            }
        }
        vm.loadProfile()


    }

    override fun onResume() {
        super.onResume()
        vm.loadProfile()
    }
}