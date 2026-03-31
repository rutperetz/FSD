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

        // 🔥 חיבור ל־XML עם findViewById
        signUpButton = findViewById(R.id.signUpButton)
        cancelButton = findViewById(R.id.cancelButton)
        viewGroupText = findViewById(R.id.viewGroupText)
        backArrow = findViewById(R.id.back_arrow)
        btnEditDeadline = findViewById(R.id.btnEditDeadline)
        btnEditGroupSize = findViewById(R.id.btnEditGroupSize)

        // קבלת נתונים
        courseId = intent.getStringExtra("courseId") ?: ""
        //studentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        //vm.loadEnrollment(courseId, studentId)
        //vm.loadCourse(courseId)

       // loadStudentAndContinue()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        vm.loadStudentAndData(userId, courseId)

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
                    Toast.makeText(this, "Search screen not implemented yet", Toast.LENGTH_SHORT).show()
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

                val deadlineTextView = findViewById<TextView>(R.id.deadlineText)
                deadlineTextView.setTextColor(getColor(android.R.color.black))

                if (course.deadline != null) {
                    deadlineTextView.text = "No deadline"
                }

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

            val intent = Intent(this, com.example.smart_group.ui.group.GroupActivity::class.java)

            intent.putExtra("courseId", courseId)
            intent.putExtra("studentId", studentId)

            startActivity(intent)
        }

        backArrow.setOnClickListener {
            finish()
        }

        btnEditDeadline.setOnClickListener {

            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker().build()

            datePicker.addOnPositiveButtonClickListener { selectedDate ->

                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = selectedDate

                // 🔥 עכשיו פותחים TimePicker
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

            val dialogView = layoutInflater.inflate(R.layout.dialog_group_size, null)

            val minInput = dialogView.findViewById<EditText>(R.id.minInput)
            val maxInput = dialogView.findViewById<EditText>(R.id.maxInput)

            val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)
            val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

            // 🔥 שליפת הערכים הנוכחיים מה-ViewModel (שמגיעים מפיירבייס)
            val currentCourse = vm.course.value

            if (currentCourse != null) {
                val min = currentCourse.groupSize.min
                val max = currentCourse.groupSize.max

                minInput.setText(min.toString())
                maxInput.setText(max.toString())

                // אופציונלי - לשים סמן בסוף
                minInput.setSelection(minInput.text.length)
                maxInput.setSelection(maxInput.text.length)
            }

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // 🔥 כפתור שמירה
            btnSave.setOnClickListener {

                val min = minInput.text.toString().toIntOrNull()
                val max = maxInput.text.toString().toIntOrNull()

                // ❌ ולידציה
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

                // 🔥 עדכון דרך ViewModel (MVVM נכון)
                vm.updateGroupSize(courseId, min, max)

                dialog.dismiss()
            }

            // 🔥 ביטול
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //UI לכפתורי הרשמה וביטול ומניעה לחיצה כפולה
    private fun updateButtonsUI(isRegistered: Boolean) {

        if (isDeadlinePassed) {
            // ❌ הדדליין עבר → שני כפתורים אפורים
            signUpButton.alpha = 0.4f
            cancelButton.alpha = 0.4f

            signUpButton.isEnabled = true   // נשאר true כדי להציג Toast
            cancelButton.isEnabled = true
            return
        }

        if (isRegistered) {
            // ✔ כבר רשום
            signUpButton.isEnabled = true
            signUpButton.alpha = 0.4f
            cancelButton.isEnabled = true
            cancelButton.alpha = 1f

        } else {
            // ✔ לא רשום
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

                // ❌ להסתיר
                signUpButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
                viewGroupText.visibility = View.GONE

                // ✅ להראות עריכה
                findViewById<ImageView>(R.id.btnEditDeadline).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.btnEditGroupSize).visibility = View.VISIBLE

            } else {

                // ✅ סטודנט
                signUpButton.visibility = View.VISIBLE
                cancelButton.visibility = View.VISIBLE
                viewGroupText.visibility = View.VISIBLE

                // ❌ בלי עריכה
                findViewById<ImageView>(R.id.btnEditDeadline).visibility = View.GONE
                findViewById<ImageView>(R.id.btnEditGroupSize).visibility = View.GONE
            }
        }
    }

//    private fun loadStudentAndContinue() {
//
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//        val repo = com.example.smart_group.data.repository.StudentRepository()
//
//        lifecycleScope.launch {
//
//            val student = repo.getStudentByUserId(userId)
//
//            if (student == null) {
//                Toast.makeText(this@CourseDetailsActivity, "Student not found", Toast.LENGTH_SHORT).show()
//                return@launch
//            }
//
//            studentId = student.studentId
//
//            vm.loadEnrollment(courseId, studentId)
//            vm.loadCourse(courseId)
//        }
//    }



}