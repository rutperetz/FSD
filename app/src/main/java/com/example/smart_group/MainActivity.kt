package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.smart_group.ui.login.LoginActivity


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Redirect immediately to LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
