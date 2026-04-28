package com.example.smart_group.ui.coursedetails

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_group.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class CourseDetailsActivity : AppCompatActivity() {

    private val vm: CourseDetailsViewModel by viewModels()
    private var courseId: String = ""
    private var studentId: String = ""
    private lateinit var signUpButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var viewGroupText: TextView
    private lateinit var backArrow: ImageView
    private lateinit var btnEditDeadline: ImageView
    private lateinit var btnEditGroupSize: ImageView
    private var isDeadlinePassed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_course_details)
        signUpButton = findViewById(R.id.signUpButton)
        cancelButton = findViewById(R.id.cancelButton)
        viewGroupText = findViewById(R.id.viewGroupText)
        backArrow = findViewById(R.id.back_arrow)
        btnEditDeadline = findViewById(R.id.btnEditDeadline)
        btnEditGroupSize = findViewById(R.id.btnEditGroupSize)

        courseId = intent.getStringExtra("courseId") ?: ""

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        vm.loadData(userId, courseId)

        setupObservers()
        setupClicks()
        setupRoleUI()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
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
    }

    private fun setupObservers() {

        vm.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        vm.isLoading.observe(this) { loading ->
            signUpButton.isEnabled = !loading
            cancelButton.isEnabled = !loading
        }
        vm.isRegistered.observe(this) { isRegistered ->
            updateButtonsUI(isRegistered)
        }
        vm.studentIdLiveData.observe(this) { id ->
            studentId = id
        }
        vm.course.observe(this) { course ->
            if (course != null) {
                findViewById<TextView>(R.id.courseTitle).text = course.title
                findViewById<TextView>(R.id.groupSizeText).text =
                    "${course.groupSize.min}-${course.groupSize.max} students"
                val image = findViewById<ImageView>(R.id.courseBanner)
                if (course.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(course.imageUrl)
                        .into(image)
                }
            }
        }
        vm.isDeadlinePassed.observe(this) { passed ->

            val deadlineTextView = findViewById<TextView>(R.id.deadlineText)
            if (passed) {
                deadlineTextView.text = "Registration closed"
                deadlineTextView.setTextColor(getColor(R.color.red))
            } else {
                val course = vm.course.value
                if (course?.deadline != null) {
                    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    deadlineTextView.text = formatter.format(course.deadline.toDate())
                }
                deadlineTextView.setTextColor(getColor(R.color.black))

            }
            isDeadlinePassed = passed
            updateButtonsUI(vm.isRegistered.value == true)
        }
    }

    private fun setupClicks() {

        signUpButton.setOnClickListener {
            if (isDeadlinePassed) {
                Toast.makeText(this, "Registration is closed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.signUp(courseId)
        }

        cancelButton.setOnClickListener {
            if (isDeadlinePassed) {
                Toast.makeText(this, "Registration is closed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.cancelSignUp(courseId)
        }

        viewGroupText.setOnClickListener {
            if (studentId.isEmpty()) {
                Toast.makeText(this, "User not ready yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isDeadlinePassed) {
                Toast.makeText(this, "Group allocation is not available yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, com.example.smart_group.ui.group.GroupActivity::class.java)
            intent.putExtra("courseId", courseId)
            intent.putExtra("studentId", studentId)
            startActivity(intent)
        }

        backArrow.setOnClickListener {
            finish()
        }

        btnEditDeadline.setOnClickListener {

            if (!isEditingAllowed()) {
                Toast.makeText(
                    this,
                    "Editing is not available during grouping",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker().build()

            datePicker.addOnPositiveButtonClickListener { selectedDate ->

                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                val timePicker = android.app.TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->

                        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(java.util.Calendar.MINUTE, minute)

                        val now = System.currentTimeMillis()

                        if (calendar.timeInMillis < now) {
                            Toast.makeText(this, "Cannot select past time", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }

                        val timestamp = com.google.firebase.Timestamp(calendar.time)

                        vm.updateDeadline(timestamp)
                    },
                    12,
                    0,
                    true
                )

                timePicker.show()
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        btnEditGroupSize.setOnClickListener {

            if (!isEditingAllowed()) {
                Toast.makeText(
                    this,
                    "Editing is not available during grouping",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_group_size, null)
            val minInput = dialogView.findViewById<EditText>(R.id.minInput)
            val maxInput = dialogView.findViewById<EditText>(R.id.maxInput)
            val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)
            val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
            val currentCourse = vm.course.value

            if (currentCourse != null) {
                val min = currentCourse.groupSize.min
                val max = currentCourse.groupSize.max

                minInput.setText(min.toString())
                maxInput.setText(max.toString())
                minInput.setSelection(minInput.text.length)
                maxInput.setSelection(maxInput.text.length)
            }

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            btnSave.setOnClickListener {

                val min = minInput.text.toString().toIntOrNull()
                val max = maxInput.text.toString().toIntOrNull()
                if (min == null || max == null) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (min <= 0 || max <= 0) {
                    Toast.makeText(this, "Values must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (min >= max) {
                    Toast.makeText(this, "Min must be smaller than Max", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                vm.updateGroupSize(courseId, min, max)
                dialog.dismiss()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun updateButtonsUI(isRegistered: Boolean) {
        if (isDeadlinePassed) {
            signUpButton.alpha = 0.4f
            cancelButton.alpha = 0.4f
            signUpButton.isEnabled = true
            cancelButton.isEnabled = true
            return
        }
        if (isRegistered) {
            signUpButton.isEnabled = true
            signUpButton.alpha = 0.4f
            cancelButton.isEnabled = true
            cancelButton.alpha = 1f
        } else {
            signUpButton.isEnabled = true
            signUpButton.alpha = 1f
            cancelButton.isEnabled = true
            cancelButton.alpha = 0.4f
        }
    }

    private fun setupRoleUI() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRepo = com.example.smart_group.data.repository.UserRepository()

        lifecycleScope.launch {
            val user = userRepo.getUser(userId) ?: return@launch
            if (user.role.name == "ADMIN") {
                signUpButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
                viewGroupText.visibility = View.GONE
                findViewById<ImageView>(R.id.btnEditDeadline).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.btnEditGroupSize).visibility = View.VISIBLE

            } else {
                signUpButton.visibility = View.VISIBLE
                cancelButton.visibility = View.VISIBLE
                viewGroupText.visibility = View.VISIBLE
                findViewById<ImageView>(R.id.btnEditDeadline).visibility = View.GONE
                findViewById<ImageView>(R.id.btnEditGroupSize).visibility = View.GONE
            }
        }
    }

    private fun isEditingAllowed(): Boolean {
        val status = vm.course.value?.groupingStatus
        return status == com.example.smart_group.data.model.GroupingStatus.PENDING ||
                status == com.example.smart_group.data.model.GroupingStatus.COMPLETED
    }
}