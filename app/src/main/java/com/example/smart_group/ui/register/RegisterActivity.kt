package com.example.smart_group.ui.register

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.smart_group.R
import com.example.smart_group.ui.login.LoginActivity
import com.example.smart_group.ui.questionnaire.QuestionnaireActivity
import com.google.android.material.button.MaterialButton

class RegisterActivity : ComponentActivity() {

    private var questionnaireCompleted: Boolean = false

    //  נשמור כאן את כל תשובות השאלון לפי הסכמה (keys קבועים)
    private var questionnaireAnswers: Map<String, Any>? = null

    private val questionnaireLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data
                val completed =
                    data?.getBooleanExtra("QUESTIONNAIRE_COMPLETED", false) ?: false

                questionnaireCompleted = completed

                val tvFillQuestionnaire = findViewById<TextView>(R.id.tv_fill_questionnaire)

                if (completed) {

                    //  SINGLE
                    val gender = data?.getStringExtra("gender") ?: ""
                    val genderPreference = data?.getStringExtra("genderPreference") ?: ""

                    // MULTI
                    val availability = data?.getStringArrayListExtra("availability") ?: arrayListOf()
                    val workStyle = data?.getStringArrayListExtra("workStyle") ?: arrayListOf()
                    val workMode = data?.getStringArrayListExtra("workMode") ?: arrayListOf()
                    val language = data?.getStringArrayListExtra("language") ?: arrayListOf()
                    val taskPreference = data?.getStringArrayListExtra("taskPreference") ?: arrayListOf()

                    //  נשמור את זה במבנה שמוכן לשמירה בפיירסטור
                    // (ניצמד לסכמה בדיוק)
                    questionnaireAnswers = mapOf(
                        "gender" to gender,
                        "genderPreference" to genderPreference,
                        "availability" to availability,
                        "workStyle" to workStyle,
                        "workMode" to workMode,
                        "language" to language,
                        "taskPreference" to taskPreference
                    )

                    tvFillQuestionnaire.text = "Questionnaire completed ✓"
                    tvFillQuestionnaire.paintFlags =
                        tvFillQuestionnaire.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()

                } else {
                    questionnaireAnswers = null

                    tvFillQuestionnaire.text = getString(R.string.fill_out_questionnaire)
                    tvFillQuestionnaire.paintFlags =
                        tvFillQuestionnaire.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
            }
        }

    private val regVm: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val usernameBox = findViewById<EditText>(R.id.username_input)
        val emailBox = findViewById<EditText>(R.id.email_input)
        val passwordBox = findViewById<EditText>(R.id.password_input)
        val signUpBtn = findViewById<MaterialButton>(R.id.signup_btn)
        val tvFillQuestionnaire = findViewById<TextView>(R.id.tv_fill_questionnaire)

        // underline like a link (until completed)
        tvFillQuestionnaire.paintFlags =
            tvFillQuestionnaire.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Navigate to questionnaire with result
        tvFillQuestionnaire.setOnClickListener {
            val intent = Intent(this, QuestionnaireActivity::class.java)
            questionnaireLauncher.launch(intent)
        }

        backArrow.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signUpBtn.setOnClickListener {

            // חובה למלא שאלון לפני הרשמה
            if (!questionnaireCompleted || questionnaireAnswers == null) {
                Toast.makeText(
                    this,
                    "Please complete the questionnaire before signing up.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            regVm.signUp(
                usernameRaw = usernameBox.text.toString(),
                emailRaw = emailBox.text.toString(),
                passwordRaw = passwordBox.text.toString(),
                questionnaireAnswers = questionnaireAnswers
            )

        }

        // MVVM errors
        regVm.usernameError.observe(this) { err -> usernameBox.error = err }
        regVm.emailError.observe(this) { err -> emailBox.error = err }
        regVm.passwordError.observe(this) { err -> passwordBox.error = err }

        regVm.toastMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                regVm.onToastShown()
            }
        }

        regVm.isLoading.observe(this) { loading ->
            signUpBtn.isEnabled = !loading
        }

        regVm.navigateToLogin.observe(this) { go ->
            if (go) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                regVm.onNavigatedToLogin()
            }
        }
    }
}
