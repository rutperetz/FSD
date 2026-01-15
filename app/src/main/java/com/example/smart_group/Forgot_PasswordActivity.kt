package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.R
import com.google.android.material.button.MaterialButton

class Forgot_PasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val BackArrow = findViewById<ImageView>(R.id.back_arrow)
        val sendCodeBtn = findViewById<MaterialButton>(R.id.send_code_btn)

        //Returns to previous screen
        BackArrow.setOnClickListener {
            finish()
        }

        //Sends code and moves to the next screen
        sendCodeBtn.setOnClickListener {
            startActivity(Intent(this, Verify_CodeActivity::class.java))
        }
    }
}
