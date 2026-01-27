package com.example.smart_group.ui.questionnaire

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smart_group.R
import android.util.Log


class QuestionnaireActivity : AppCompatActivity() {

    private val vm: QuestionnaireViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // multi-select toggle (exclude gender + genderPreference because they're RadioGroup singles)
        val multiIds = listOf(
            R.id.rb_av_morning, R.id.rb_av_afternoon, R.id.rb_av_evening, R.id.rb_av_weekend,
            R.id.rb_style_individual, R.id.rb_style_collaborative,
            R.id.rb_mode_on_campus, R.id.rb_mode_remote,
            R.id.rb_lang_hebrew, R.id.rb_lang_english, R.id.rb_lang_arabic,
            R.id.rb_task_fixed, R.id.rb_task_flexible
        )

        multiIds.forEach { id ->
            val rb = findViewById<RadioButton>(id)
            rb.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    rb.isChecked = !rb.isChecked
                    true
                } else false
            }
        }

        vm.state.observe(this) { st ->
            if (st is QuestionnaireUiState.Saved) {
                val data = Intent().apply {
                    putExtra("QUESTIONNAIRE_COMPLETED", true)

                    // אם תרצי להעביר גם את כל התשובות ל-Register:
                    putExtra("gender", st.answers.gender)
                    putExtra("genderPreference", st.answers.genderPreference)
                    putStringArrayListExtra("availability", ArrayList(st.answers.availability))
                    putStringArrayListExtra("workStyle", ArrayList(st.answers.workStyle))
                    putStringArrayListExtra("workMode", ArrayList(st.answers.workMode))
                    putStringArrayListExtra("language", ArrayList(st.answers.language))
                    putStringArrayListExtra("taskPreference", ArrayList(st.answers.taskPreference))
                }
                setResult(Activity.RESULT_OK, data)
                vm.reset()
                finish()
            }
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            clearAllErrors()

            // Gender (tag)
            val genderId = findViewById<RadioGroup>(R.id.rg_gender).checkedRadioButtonId
            val gender = if (genderId != -1) findViewById<RadioButton>(genderId).tag.toString() else ""

            // Gender Preference (tag) - single
            val prefId = findViewById<RadioGroup>(R.id.rg_gender_pref).checkedRadioButtonId
            val genderPreference = if (prefId != -1) findViewById<RadioButton>(prefId).tag.toString() else ""

            fun checkedTags(vararg ids: Int): List<String> {
                val out = mutableListOf<String>()
                ids.forEach { id ->
                    val rb = findViewById<RadioButton>(id)
                    if (rb.isChecked) out.add(rb.tag.toString())
                }
                return out
            }

            val answers = QuestionnaireAnswers(
                gender = gender,
                genderPreference = genderPreference,
                availability = checkedTags(R.id.rb_av_morning, R.id.rb_av_afternoon, R.id.rb_av_evening, R.id.rb_av_weekend),
                workStyle = checkedTags(R.id.rb_style_individual, R.id.rb_style_collaborative),
                workMode = checkedTags(R.id.rb_mode_on_campus, R.id.rb_mode_remote),
                language = checkedTags(R.id.rb_lang_hebrew, R.id.rb_lang_english, R.id.rb_lang_arabic),
                taskPreference = checkedTags(R.id.rb_task_fixed, R.id.rb_task_flexible)
            )

            // required red flags
            setFieldError(R.id.tv_gender_title, R.id.tv_gender_required, answers.gender.isBlank())
            setFieldError(R.id.tv_gender_pref_title, R.id.tv_gender_pref_required, answers.genderPreference.isBlank())
            setFieldError(R.id.tv_availability_title, R.id.tv_availability_required, answers.availability.isEmpty())
            setFieldError(R.id.tv_work_style_title, R.id.tv_work_style_required, answers.workStyle.isEmpty())
            setFieldError(R.id.tv_work_mode_title, R.id.tv_work_mode_required, answers.workMode.isEmpty())
            setFieldError(R.id.tv_language_title, R.id.tv_language_required, answers.language.isEmpty())
            setFieldError(R.id.tv_task_title, R.id.tv_task_required, answers.taskPreference.isEmpty())

            android.util.Log.d("Questionnaire", "answers = $answers")

            if (!vm.validate(answers)) {
                Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_LONG).show()
                findViewById<ScrollView>(R.id.sv_content).smoothScrollTo(0, 0)
                return@setOnClickListener
            }

            vm.saveLocal(answers)
        }
    }

    private fun setFieldError(titleId: Int, requiredId: Int, hasError: Boolean) {
        val title = findViewById<TextView>(titleId)
        val required = findViewById<TextView>(requiredId)

        if (hasError) {
            title.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            required.visibility = android.view.View.VISIBLE
        } else {
            title.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            required.visibility = android.view.View.GONE
        }
    }

    private fun clearAllErrors() {
        setFieldError(R.id.tv_gender_title, R.id.tv_gender_required, false)
        setFieldError(R.id.tv_gender_pref_title, R.id.tv_gender_pref_required, false)
        setFieldError(R.id.tv_availability_title, R.id.tv_availability_required, false)
        setFieldError(R.id.tv_work_style_title, R.id.tv_work_style_required, false)
        setFieldError(R.id.tv_work_mode_title, R.id.tv_work_mode_required, false)
        setFieldError(R.id.tv_language_title, R.id.tv_language_required, false)
        setFieldError(R.id.tv_task_title, R.id.tv_task_required, false)
    }
}
