package com.example.smart_group.ui.courses

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.smart_group.R

class StatsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val tvTotalCourses = findViewById<TextView>(R.id.tvTotalCourses)
        val tvMathCount = findViewById<TextView>(R.id.tvMathCount)
        val tvCodeCount = findViewById<TextView>(R.id.tvCodeCount)
        val tvCsCount = findViewById<TextView>(R.id.tvCsCount)

        val progressMath = findViewById<ProgressBar>(R.id.progressMath)
        val progressCode = findViewById<ProgressBar>(R.id.progressCode)
        val progressCs = findViewById<ProgressBar>(R.id.progressCs)

        val mathCount = 2
        val codeCount = 3
        val csCount = 1
        val total = mathCount + codeCount + csCount

        tvTotalCourses.text = "Total Courses: $total"
        tvMathCount.text = "מתמטיקה: $mathCount"
        tvCodeCount.text = "שפות תכנות: $codeCount"
        tvCsCount.text = "מדעי המחשב: $csCount"

        progressMath.max = total
        progressCode.max = total
        progressCs.max = total

        progressMath.progress = mathCount
        progressCode.progress = codeCount
        progressCs.progress = csCount
    }
}