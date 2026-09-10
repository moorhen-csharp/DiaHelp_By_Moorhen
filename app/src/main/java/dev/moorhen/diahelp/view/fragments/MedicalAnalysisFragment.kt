package dev.moorhen.diahelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.data.model.MedicalAnalysisModel
import dev.moorhen.diahelp.viewmodel.MedicalIndicationsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MedicalAnalysisFragment : Fragment() {

    companion object {
        private const val ARG_TYPE  = "analysis_type"
        private const val ARG_TITLE = "analysis_title"

        fun newInstance(type: String, title: String) = MedicalAnalysisFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TYPE, type)
                putString(ARG_TITLE, title)
            }
        }
    }

    private lateinit var viewModel: MedicalIndicationsViewModel
    private lateinit var root: View

    // Режимы
    private lateinit var viewModeContainer: View
    private lateinit var editModeContainer: View

    // Просмотр
    private lateinit var viewTitleText: TextView
    private lateinit var viewDateText: TextView
    private lateinit var emptyStateView: View
    private lateinit var dataView: View
    private lateinit var btnEdit: MaterialButton

    // Карточки просмотра
    private lateinit var cardHba1cView: MaterialCardView
    private lateinit var cardBloodView: MaterialCardView
    private lateinit var cardLipidsView: MaterialCardView
    private lateinit var cardBiochemView: MaterialCardView

    // Строки просмотра
    private lateinit var rowCpeptide: View
    private lateinit var rowHemoglobin: View
    private lateinit var rowLeukocytes: View
    private lateinit var rowPlatelets: View
    private lateinit var rowCholesterol: View
    private lateinit var rowHdl: View
    private lateinit var rowLdl: View
    private lateinit var rowTriglycerides: View
    private lateinit var rowCreatinine: View
    private lateinit var rowUrea: View
    private lateinit var rowAlt: View
    private lateinit var rowAst: View
    private lateinit var tvHba1cValue: TextView

    // Редактирование
    private lateinit var editTitleText: TextView
    private lateinit var btnCancelEdit: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var hba1cInput: TextInputEditText

    // Карточки редактирования (для скрытия по типу анализа)
    private lateinit var cardHba1cEdit: MaterialCardView
    private lateinit var cardBloodEdit: MaterialCardView
    private lateinit var cardLipidsEdit: MaterialCardView
    private lateinit var cardBiochemEdit: MaterialCardView

    private var currentType: String = "blood"

    // Строки редактирования
    private lateinit var editCpeptide: View
    private lateinit var editHemoglobin: View
    private lateinit var editLeukocytes: View
    private lateinit var editPlatelets: View
    private lateinit var editCholesterol: View
    private lateinit var editHdl: View
    private lateinit var editLdl: View
    private lateinit var editTriglycerides: View
    private lateinit var editCreatinine: View
    private lateinit var editUrea: View
    private lateinit var editAlt: View
    private lateinit var editAst: View

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_medical_analysis, container, false)
        viewModel = ViewModelProvider(this)[MedicalIndicationsViewModel::class.java]

        bindViews()
        setupContent()
        setupListeners()
        observeViewModel()

        return root
    }

    private fun bindViews() {
        // Контейнеры режимов
        viewModeContainer = root.findViewById(R.id.viewModeContainer)
        editModeContainer = root.findViewById(R.id.editModeContainer)

        // Просмотр — шапка
        viewTitleText = root.findViewById(R.id.viewTitleText)
        viewDateText  = root.findViewById(R.id.viewDateText)
        emptyStateView = root.findViewById(R.id.emptyStateView)
        dataView      = root.findViewById(R.id.dataView)
        btnEdit       = root.findViewById(R.id.btnEdit)

        // Просмотр — карточки
        cardHba1cView  = root.findViewById(R.id.cardHba1cView)
        cardBloodView  = root.findViewById(R.id.cardBloodView)
        cardLipidsView = root.findViewById(R.id.cardLipidsView)
        cardBiochemView = root.findViewById(R.id.cardBiochemView)
        tvHba1cValue   = root.findViewById(R.id.tvHba1cValue)

        // Просмотр — строки
        rowCpeptide    = root.findViewById(R.id.rowCpeptide)
        rowHemoglobin  = root.findViewById(R.id.rowHemoglobin)
        rowLeukocytes  = root.findViewById(R.id.rowLeukocytes)
        rowPlatelets   = root.findViewById(R.id.rowPlatelets)
        rowCholesterol = root.findViewById(R.id.rowCholesterol)
        rowHdl         = root.findViewById(R.id.rowHdl)
        rowLdl         = root.findViewById(R.id.rowLdl)
        rowTriglycerides = root.findViewById(R.id.rowTriglycerides)
        rowCreatinine  = root.findViewById(R.id.rowCreatinine)
        rowUrea        = root.findViewById(R.id.rowUrea)
        rowAlt         = root.findViewById(R.id.rowAlt)
        rowAst         = root.findViewById(R.id.rowAst)

        // Редактирование — шапка
        editTitleText  = root.findViewById(R.id.editTitleText)
        btnCancelEdit  = root.findViewById(R.id.btnCancelEdit)
        btnSave        = root.findViewById(R.id.btnSave)
        hba1cInput     = root.findViewById(R.id.hba1cInput)

        // Карточки редактирования
        cardHba1cEdit  = root.findViewById(R.id.cardHba1cEdit)
        cardBloodEdit  = root.findViewById(R.id.cardBloodEdit)
        cardLipidsEdit = root.findViewById(R.id.cardLipidsEdit)
        cardBiochemEdit = root.findViewById(R.id.cardBiochemEdit)

        // Редактирование — строки
        editCpeptide    = root.findViewById(R.id.editCpeptide)
        editHemoglobin  = root.findViewById(R.id.editHemoglobin)
        editLeukocytes  = root.findViewById(R.id.editLeukocytes)
        editPlatelets   = root.findViewById(R.id.editPlatelets)
        editCholesterol = root.findViewById(R.id.editCholesterol)
        editHdl         = root.findViewById(R.id.editHdl)
        editLdl         = root.findViewById(R.id.editLdl)
        editTriglycerides = root.findViewById(R.id.editTriglycerides)
        editCreatinine  = root.findViewById(R.id.editCreatinine)
        editUrea        = root.findViewById(R.id.editUrea)
        editAlt         = root.findViewById(R.id.editAlt)
        editAst         = root.findViewById(R.id.editAst)
    }

    private fun setupContent() {
        val type  = arguments?.getString(ARG_TYPE)  ?: "blood"
        val title = arguments?.getString(ARG_TITLE) ?: "Анализы"
        currentType = type

        viewTitleText.text = title
        editTitleText.text = "Редактирование"

        applyTypeVisibility(type)

        // Настраиваем метки и нормы для строк просмотра
        setupViewRow(rowCpeptide,    "C-пептид",         "0.5–2.0 нг/мл")
        setupViewRow(rowHemoglobin,  "Гемоглобин",       "120–160 г/л")
        setupViewRow(rowLeukocytes,  "Лейкоциты",        "4.0–9.0 ×10⁹/л")
        setupViewRow(rowPlatelets,   "Тромбоциты",       "150–400 ×10⁹/л")
        setupViewRow(rowCholesterol, "Общий холестерин", "< 5.0 ммоль/л")
        setupViewRow(rowHdl,         "ЛПВП",             "> 1.0 ммоль/л")
        setupViewRow(rowLdl,         "ЛПНП",             "< 2.6 ммоль/л")
        setupViewRow(rowTriglycerides, "Триглицериды",   "< 1.7 ммоль/л")
        setupViewRow(rowCreatinine,  "Креатинин",        "44–115 мкмоль/л")
        setupViewRow(rowUrea,        "Мочевина",         "2.5–8.3 ммоль/л")
        setupViewRow(rowAlt,         "АЛТ",              "< 40 Ед/л")
        setupViewRow(rowAst,         "АСТ",              "< 40 Ед/л")

        // Настраиваем метки, нормы и единицы для строк редактирования
        setupEditRow(editCpeptide,    "C-пептид",         "0.5–2.0 нг/мл",   "нг/мл")
        setupEditRow(editHemoglobin,  "Гемоглобин",       "120–160 г/л",      "г/л")
        setupEditRow(editLeukocytes,  "Лейкоциты",        "4.0–9.0 ×10⁹/л",  "×10⁹/л")
        setupEditRow(editPlatelets,   "Тромбоциты",       "150–400 ×10⁹/л",  "×10⁹/л")
        setupEditRow(editCholesterol, "Общий холестерин", "< 5.0 ммоль/л",    "ммоль/л")
        setupEditRow(editHdl,         "ЛПВП",             "> 1.0 ммоль/л",    "ммоль/л")
        setupEditRow(editLdl,         "ЛПНП",             "< 2.6 ммоль/л",    "ммоль/л")
        setupEditRow(editTriglycerides, "Триглицериды",   "< 1.7 ммоль/л",   "ммоль/л")
        setupEditRow(editCreatinine,  "Креатинин",        "44–115 мкмоль/л",  "мкмоль/л")
        setupEditRow(editUrea,        "Мочевина",         "2.5–8.3 ммоль/л",  "ммоль/л")
        setupEditRow(editAlt,         "АЛТ",              "< 40 Ед/л",        "Ед/л")
        setupEditRow(editAst,         "АСТ",              "< 40 Ед/л",        "Ед/л")

        viewModel.loadLatest(type)
    }

    /**
     * Скрывает карточки, не относящиеся к текущему типу анализа, чтобы, например,
     * в "Липидном профиле" не показывались показатели крови или биохимия.
     * Карточки, относящиеся к типу, дальше управляются showData()/switchToEditMode()
     * в зависимости от наличия данных.
     */
    private fun applyTypeVisibility(type: String) {
        val showHba1c   = type == "blood"
        val showBlood   = type == "blood"
        val showLipids  = type == "lipids"
        val showBiochem = type == "biochem"

        if (!showHba1c)   cardHba1cView.visibility = View.GONE
        if (!showBlood)   cardBloodView.visibility = View.GONE
        if (!showLipids)  cardLipidsView.visibility = View.GONE
        if (!showBiochem) cardBiochemView.visibility = View.GONE

        cardHba1cEdit.visibility   = if (showHba1c) View.VISIBLE else View.GONE
        cardBloodEdit.visibility   = if (showBlood) View.VISIBLE else View.GONE
        cardLipidsEdit.visibility  = if (showLipids) View.VISIBLE else View.GONE
        cardBiochemEdit.visibility = if (showBiochem) View.VISIBLE else View.GONE
    }

    private fun setupViewRow(row: View, label: String, norm: String) {
        row.findViewById<TextView>(R.id.tvRowLabel).text = label
        row.findViewById<TextView>(R.id.tvRowNorm).text  = "Норма: $norm"
    }

    private fun setupEditRow(row: View, label: String, norm: String, unit: String) {
        row.findViewById<TextView>(R.id.tvEditLabel).text = label
        row.findViewById<TextView>(R.id.tvEditNorm).text  = "Норма: $norm"
        row.findViewById<TextInputLayout>(R.id.tilEditValue).suffixText = unit
    }

    private fun setupListeners() {
        btnEdit.setOnClickListener { switchToEditMode() }
        btnCancelEdit.setOnClickListener { switchToViewMode() }

        btnSave.setOnClickListener {
            val type = currentType

            val hba1c       = if (type == "blood")   hba1cInput.text.toString().toDoubleOrNull() else null
            val cpeptide    = if (type == "blood")   editCpeptide.et().toDoubleOrNull() else null
            val hemoglobin  = if (type == "blood")   editHemoglobin.et().toDoubleOrNull() else null
            val leukocytes  = if (type == "blood")   editLeukocytes.et().toDoubleOrNull() else null
            val platelets   = if (type == "blood")   editPlatelets.et().toDoubleOrNull() else null
            val cholesterol = if (type == "lipids")  editCholesterol.et().toDoubleOrNull() else null
            val hdl         = if (type == "lipids")  editHdl.et().toDoubleOrNull() else null
            val ldl         = if (type == "lipids")  editLdl.et().toDoubleOrNull() else null
            val triglyc     = if (type == "lipids")  editTriglycerides.et().toDoubleOrNull() else null
            val creatinine  = if (type == "biochem") editCreatinine.et().toDoubleOrNull() else null
            val urea        = if (type == "biochem") editUrea.et().toDoubleOrNull() else null
            val alt         = if (type == "biochem") editAlt.et().toDoubleOrNull() else null
            val ast         = if (type == "biochem") editAst.et().toDoubleOrNull() else null

            val anyFilled = listOf(hba1c, cpeptide, hemoglobin, leukocytes, platelets,
                cholesterol, hdl, ldl, triglyc, creatinine, urea, alt, ast).any { it != null }

            if (!anyFilled) {
                Toast.makeText(requireContext(), "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveAnalysis(type, hba1c, cpeptide, hemoglobin, leukocytes, platelets,
                cholesterol, hdl, ldl, triglyc, creatinine, urea, alt, ast)
        }
    }

    /** Читает текст из TextInputEditText внутри include-строки */
    private fun View.et(): String =
        this.findViewById<TextInputEditText>(R.id.etEditValue).text.toString()

    private fun observeViewModel() {
        viewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            if (record == null) {
                showEmptyState()
            } else {
                showData(record)
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "✅ Анализы сохранены", Toast.LENGTH_SHORT).show()
                switchToViewMode()
                viewModel.loadLatest(currentType)
            }
        }
    }

    private fun showEmptyState() {
        dataView.visibility      = View.GONE
        emptyStateView.visibility = View.VISIBLE
        viewDateText.text        = "Данные ещё не вводились"
    }

    private fun showData(record: MedicalAnalysisModel) {
        emptyStateView.visibility = View.GONE
        dataView.visibility       = View.VISIBLE
        viewDateText.text         = "Обновлено: ${dateFormat.format(record.date)}"

        // HbA1c — акцентная карточка
        if (currentType == "blood" && record.hba1c != null) {
            cardHba1cView.visibility = View.VISIBLE
            tvHba1cValue.text = "${record.hba1c} %"
            val status = when {
                record.hba1c < 7.0  -> "✅ В норме"
                record.hba1c < 8.0  -> "⚠️ Немного выше нормы"
                else                -> "❗ Выше нормы"
            }
            root.findViewById<TextView>(R.id.tvHba1cStatus)?.text = status
        } else {
            cardHba1cView.visibility = View.GONE
        }

        // Показатели крови
        val bloodAny = currentType == "blood" && listOf(record.cpeptide, record.hemoglobin,
            record.leukocytes, record.platelets).any { it != null }
        cardBloodView.visibility = if (bloodAny) View.VISIBLE else View.GONE
        setViewRow(rowCpeptide,   record.cpeptide,   "нг/мл")
        setViewRow(rowHemoglobin, record.hemoglobin, "г/л")
        setViewRow(rowLeukocytes, record.leukocytes, "×10⁹/л")
        setViewRow(rowPlatelets,  record.platelets,  "×10⁹/л")

        // Липиды
        val lipidsAny = currentType == "lipids" && listOf(record.cholesterol, record.hdl,
            record.ldl, record.triglycerides).any { it != null }
        cardLipidsView.visibility = if (lipidsAny) View.VISIBLE else View.GONE
        setViewRow(rowCholesterol,   record.cholesterol,  "ммоль/л")
        setViewRow(rowHdl,           record.hdl,          "ммоль/л")
        setViewRow(rowLdl,           record.ldl,          "ммоль/л")
        setViewRow(rowTriglycerides, record.triglycerides,"ммоль/л")

        // Биохимия
        val biochemAny = currentType == "biochem" && listOf(record.creatinine, record.urea,
            record.alt, record.ast).any { it != null }
        cardBiochemView.visibility = if (biochemAny) View.VISIBLE else View.GONE
        setViewRow(rowCreatinine, record.creatinine, "мкмоль/л")
        setViewRow(rowUrea,       record.urea,       "ммоль/л")
        setViewRow(rowAlt,        record.alt,        "Ед/л")
        setViewRow(rowAst,        record.ast,        "Ед/л")
    }

    /** Заполняет строку просмотра значением и единицей, либо скрывает если null */
    private fun setViewRow(row: View, value: Double?, unit: String) {
        if (value == null) {
            row.visibility = View.GONE
            return
        }
        row.visibility = View.VISIBLE
        // Скрываем разделитель предыдущего GONE-элемента через tag не получится красиво,
        // поэтому показываем всё — divider'ы всегда видны внутри карточки
        val formatted = if (value == value.toLong().toDouble()) value.toLong().toString()
        else value.toString()
        row.findViewById<TextView>(R.id.tvRowValue).text = "$formatted $unit"
    }

    private fun switchToEditMode() {
        // Заполняем поля текущими значениями из последней записи
        val record = viewModel.latestRecord.value
        if (record != null) {
            when (currentType) {
                "blood" -> {
                    hba1cInput.setText(record.hba1c?.toString() ?: "")
                    setEditField(editCpeptide,    record.cpeptide)
                    setEditField(editHemoglobin,  record.hemoglobin)
                    setEditField(editLeukocytes,  record.leukocytes)
                    setEditField(editPlatelets,   record.platelets)
                }
                "lipids" -> {
                    setEditField(editCholesterol, record.cholesterol)
                    setEditField(editHdl,         record.hdl)
                    setEditField(editLdl,         record.ldl)
                    setEditField(editTriglycerides, record.triglycerides)
                }
                "biochem" -> {
                    setEditField(editCreatinine,  record.creatinine)
                    setEditField(editUrea,        record.urea)
                    setEditField(editAlt,         record.alt)
                    setEditField(editAst,         record.ast)
                }
            }
        }

        viewModeContainer.visibility = View.GONE
        editModeContainer.visibility = View.VISIBLE
    }

    private fun switchToViewMode() {
        editModeContainer.visibility = View.GONE
        viewModeContainer.visibility = View.VISIBLE
    }

    private fun setEditField(row: View, value: Double?) {
        val et = row.findViewById<TextInputEditText>(R.id.etEditValue)
        et.setText(if (value != null) {
            if (value == value.toLong().toDouble()) value.toLong().toString()
            else value.toString()
        } else "")
    }
}
