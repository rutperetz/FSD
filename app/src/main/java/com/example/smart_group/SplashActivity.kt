package com.example.smart_group

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.ui.group.GroupActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.widget.ImageView>(R.id.logo)

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1500
            fillAfter = true
        }

        val scale = ScaleAnimation(
            0.8f, 1f,
            0.8f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1500
            fillAfter = true
        }

        logo.startAnimation(fadeIn)
        logo.startAnimation(scale)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("courseId", "C1")
            intent.putExtra("studentId", "S1")
            startActivity(intent)
            finish()
        }, 3000)
    }
}