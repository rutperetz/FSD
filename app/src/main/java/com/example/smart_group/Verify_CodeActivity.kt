package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Verify_CodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val continueBtn = findViewById<MaterialButton>(R.id.continue_btn)

        //Returns to previous screen
        backArrow.setOnClickListener {
            finish()
        }

        //check code and moves to the next screen
        continueBtn.setOnClickListener {
            startActivity(Intent(this, Set_New_PasswordActivity::class.java))
        }
    }
}
