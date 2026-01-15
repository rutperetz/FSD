package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.R
import com.google.android.material.button.MaterialButton

class Set_New_PasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_new_password)

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val saveBtn = findViewById<MaterialButton>(R.id.save_new_password_btn)

        backArrow.setOnClickListener { finish() }

        saveBtn.setOnClickListener {
            // TODO: later connect to backend and validate passwords
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
