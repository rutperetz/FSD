package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity




class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Redirect immediately to LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
