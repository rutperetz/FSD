package com.example.smart_group.ui.questionnaire

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smart_group.R
import com.example.smart_group.data.model.Answers

class EditQuestionnaireActivity : AppCompatActivity() {

    private val vm: EditQuestionnaireViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)

        setupHeader()
        setupMultiSelectToggle()
        observeViewModel()

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            onSaveClicked()
        }

        vm.loadCurrentUserAnswers()
    }

    private fun setupHeader() {
        findViewById<ImageView>(R.id.back_arrow).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_title).text = "Edit Questionnaire"
    }

    private fun setupMultiSelectToggle() {
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
                } else {
                    false
                }
            }
        }
    }

    private fun observeViewModel() {
        vm.state.observe(this) { state ->
            when (state) {
                is EditQuestionnaireUiState.Idle -> Unit

                is EditQuestionnaireUiState.Loading -> {
                    findViewById<Button>(R.id.btn_save).isEnabled = false
                }

                is EditQuestionnaireUiState.Loaded -> {
                    findViewById<Button>(R.id.btn_save).isEnabled = true
                    fillUiFromAnswers(state.answers)
                }

                is EditQuestionnaireUiState.Saved -> {
                    findViewById<Button>(R.id.btn_save).isEnabled = true
                    Toast.makeText(this, "Questionnaire updated successfully", Toast.LENGTH_LONG).show()
                    finish()
                }

                is EditQuestionnaireUiState.Error -> {
                    findViewById<Button>(R.id.btn_save).isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onSaveClicked() {
        clearAllErrors()

        val answers = collectAnswersFromUi()

        setFieldError(
            titleId = R.id.tv_gender_title,
            requiredId = R.id.tv_gender_required,
            hasError = answers.gender.isBlank()
        )

        setFieldError(
            titleId = R.id.tv_gender_pref_title,
            requiredId = R.id.tv_gender_pref_required,
            hasError = answers.genderPreference.isBlank()
        )

        setFieldError(
            titleId = R.id.tv_availability_title,
            requiredId = R.id.tv_availability_required,
            hasError = answers.availability.isEmpty()
        )

        setFieldError(
            titleId = R.id.tv_work_style_title,
            requiredId = R.id.tv_work_style_required,
            hasError = answers.workStyle.isEmpty()
        )

        setFieldError(
            titleId = R.id.tv_work_mode_title,
            requiredId = R.id.tv_work_mode_required,
            hasError = answers.workMode.isEmpty()
        )

        setFieldError(
            titleId = R.id.tv_language_title,
            requiredId = R.id.tv_language_required,
            hasError = answers.language.isEmpty()
        )

        setFieldError(
            titleId = R.id.tv_task_title,
            requiredId = R.id.tv_task_required,
            hasError = answers.taskPreference.isEmpty()
        )

        if (!vm.validate(answers)) {
            Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_LONG).show()
            findViewById<ScrollView>(R.id.sv_content).smoothScrollTo(0, 0)
            return
        }

        vm.saveAnswers(answers)
    }

    private fun collectAnswersFromUi(): Answers {
        val genderId = findViewById<RadioGroup>(R.id.rg_gender).checkedRadioButtonId
        val gender =
            if (genderId != -1) findViewById<RadioButton>(genderId).tag.toString() else ""

        val prefId = findViewById<RadioGroup>(R.id.rg_gender_pref).checkedRadioButtonId
        val genderPreference =
            if (prefId != -1) findViewById<RadioButton>(prefId).tag.toString() else ""

        fun checkedTags(vararg ids: Int): List<String> {
            val out = mutableListOf<String>()
            ids.forEach { id ->
                val rb = findViewById<RadioButton>(id)
                if (rb.isChecked) out.add(rb.tag.toString())
            }
            return out
        }

        return Answers(
            gender = gender,
            genderPreference = genderPreference,
            availability = checkedTags(
                R.id.rb_av_morning,
                R.id.rb_av_afternoon,
                R.id.rb_av_evening,
                R.id.rb_av_weekend
            ),
            workStyle = checkedTags(
                R.id.rb_style_individual,
                R.id.rb_style_collaborative
            ),
            workMode = checkedTags(
                R.id.rb_mode_on_campus,
                R.id.rb_mode_remote
            ),
            language = checkedTags(
                R.id.rb_lang_hebrew,
                R.id.rb_lang_english,
                R.id.rb_lang_arabic
            ),
            taskPreference = checkedTags(
                R.id.rb_task_fixed,
                R.id.rb_task_flexible
            )
        )
    }

    private fun fillUiFromAnswers(answers: Answers) {
        clearSelections()

        when (answers.gender) {
            "male" -> findViewById<RadioButton>(R.id.rb_gender_male).isChecked = true
            "female" -> findViewById<RadioButton>(R.id.rb_gender_female).isChecked = true
        }

        when (answers.genderPreference) {
            "men" -> findViewById<RadioButton>(R.id.rb_pref_men).isChecked = true
            "women" -> findViewById<RadioButton>(R.id.rb_pref_women).isChecked = true
            "no_preference" -> findViewById<RadioButton>(R.id.rb_pref_none).isChecked = true
        }

        setMultiChecked(
            answers.availability,
            mapOf(
                "morning" to R.id.rb_av_morning,
                "afternoon" to R.id.rb_av_afternoon,
                "evening" to R.id.rb_av_evening,
                "weekend" to R.id.rb_av_weekend
            )
        )

        setMultiChecked(
            answers.workStyle,
            mapOf(
                "individual" to R.id.rb_style_individual,
                "collaborative" to R.id.rb_style_collaborative
            )
        )

        setMultiChecked(
            answers.workMode,
            mapOf(
                "oncampus" to R.id.rb_mode_on_campus,
                "remote" to R.id.rb_mode_remote
            )
        )

        setMultiChecked(
            answers.language,
            mapOf(
                "Hebrew" to R.id.rb_lang_hebrew,
                "English" to R.id.rb_lang_english,
                "Arabic" to R.id.rb_lang_arabic
            )
        )

        setMultiChecked(
            answers.taskPreference,
            mapOf(
                "fixed" to R.id.rb_task_fixed,
                "flexible" to R.id.rb_task_flexible
            )
        )
    }

    private fun setMultiChecked(selectedValues: List<String>, valueToIdMap: Map<String, Int>) {
        selectedValues.forEach { value ->
            valueToIdMap[value]?.let { id ->
                findViewById<RadioButton>(id).isChecked = true
            }
        }
    }

    private fun clearSelections() {
        findViewById<RadioGroup>(R.id.rg_gender).clearCheck()
        findViewById<RadioGroup>(R.id.rg_gender_pref).clearCheck()

        val multiIds = listOf(
            R.id.rb_av_morning, R.id.rb_av_afternoon, R.id.rb_av_evening, R.id.rb_av_weekend,
            R.id.rb_style_individual, R.id.rb_style_collaborative,
            R.id.rb_mode_on_campus, R.id.rb_mode_remote,
            R.id.rb_lang_hebrew, R.id.rb_lang_english, R.id.rb_lang_arabic,
            R.id.rb_task_fixed, R.id.rb_task_flexible
        )

        multiIds.forEach { id ->
            findViewById<RadioButton>(id).isChecked = false
        }
    }

    private fun setFieldError(titleId: Int, requiredId: Int, hasError: Boolean) {
        val title = findViewById<TextView>(titleId)
        val required = findViewById<TextView>(requiredId)

        if (hasError) {
            title.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            required.visibility = View.VISIBLE
        } else {
            title.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            required.visibility = View.GONE
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