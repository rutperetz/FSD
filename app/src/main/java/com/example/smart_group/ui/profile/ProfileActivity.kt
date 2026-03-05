package com.example.smart_group.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smart_group.R
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var vm: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        vm = ViewModelProvider(this)[ProfileViewModel::class.java]

        // NEW UI ids (from activity_profile.xml)
        val tvUserName = findViewById<TextView>(R.id.tv_user_name)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val btnEditProfile = findViewById<MaterialButton>(R.id.btn_edit_profile)
        val btnQuestionnaire = findViewById<MaterialButton>(R.id.btn_view_questionnaire)

        // Navigate to Edit Profile
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Questionnaire screen doesn't exist yet
        btnQuestionnaire.setOnClickListener {
            // TODO: create QuestionnaireEditActivity and navigate here
            Toast.makeText(this, "Questionnaire screen not implemented yet", Toast.LENGTH_SHORT).show()
        }

        // Observe data from VM
        vm.state.observe(this) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    // optional: show loader
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

        vm.loadProfile()
    }

    override fun onResume() {
        super.onResume()
        // After coming back from EditProfile, refresh
        vm.loadProfile()
    }
}