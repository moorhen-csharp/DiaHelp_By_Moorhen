package dev.moorhen.diahelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.viewmodel.MedicalIndicationsViewModel

class MedicalAnalysisFragment : Fragment() {

    companion object {
        private const val ARG_TYPE = "analysis_type"
        private const val ARG_TITLE = "analysis_title"

        fun newInstance(type: String, title: String) = MedicalAnalysisFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TYPE, type)
                putString(ARG_TITLE, title)
            }
        }
    }

    private lateinit var viewModel: MedicalIndicationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_medical_analysis, container, false)

        viewModel = ViewModelProvider(this)[MedicalIndicationsViewModel::class.java]

        val type = arguments?.getString(ARG_TYPE) ?: "blood"
        val title = arguments?.getString(ARG_TITLE) ?: "Анализы"

        // Устанавливаем заголовок
        view.findViewById<android.widget.TextView>(R.id.titleText).text = title

        // Поля ввода
        val hba1cInput       = view.findViewById<TextInputEditText>(R.id.hba1cInput)
        val cpeptideInput    = view.findViewById<TextInputEditText>(R.id.cpeptideInput)
        val hemoglobinInput  = view.findViewById<TextInputEditText>(R.id.hemoglobinInput)
        val leukocytesInput  = view.findViewById<TextInputEditText>(R.id.leukocytesInput)
        val plateletsInput   = view.findViewById<TextInputEditText>(R.id.plateletsInput)
        val cholesterolInput = view.findViewById<TextInputEditText>(R.id.cholesterolInput)
        val hdlInput         = view.findViewById<TextInputEditText>(R.id.hdlInput)
        val ldlInput         = view.findViewById<TextInputEditText>(R.id.ldlInput)
        val triglycInput     = view.findViewById<TextInputEditText>(R.id.triglyceridesInput)
        val creatinineInput  = view.findViewById<TextInputEditText>(R.id.creatinineInput)
        val ureaInput        = view.findViewById<TextInputEditText>(R.id.ureaInput)
        val altInput         = view.findViewById<TextInputEditText>(R.id.altInput)
        val astInput         = view.findViewById<TextInputEditText>(R.id.astInput)

        // Загружаем последние значения для подсказки
        viewModel.loadLatest(type)
        viewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            record ?: return@observe
            // Заполняем поля последними сохранёнными значениями как hint
            record.hba1c?.let { hba1cInput.hint = it.toString() }
            record.cpeptide?.let { cpeptideInput.hint = it.toString() }
            record.hemoglobin?.let { hemoglobinInput.hint = it.toString() }
        }

        // Наблюдаем за результатом сохранения
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "✅ Анализы сохранены", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        view.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            // Проверяем — хоть одно поле должно быть заполнено
            val hba1c       = hba1cInput.text.toString().toDoubleOrNull()
            val cpeptide    = cpeptideInput.text.toString().toDoubleOrNull()
            val hemoglobin  = hemoglobinInput.text.toString().toDoubleOrNull()
            val leukocytes  = leukocytesInput.text.toString().toDoubleOrNull()
            val platelets   = plateletsInput.text.toString().toDoubleOrNull()
            val cholesterol = cholesterolInput.text.toString().toDoubleOrNull()
            val hdl         = hdlInput.text.toString().toDoubleOrNull()
            val ldl         = ldlInput.text.toString().toDoubleOrNull()
            val triglyc     = triglycInput.text.toString().toDoubleOrNull()
            val creatinine  = creatinineInput.text.toString().toDoubleOrNull()
            val urea        = ureaInput.text.toString().toDoubleOrNull()
            val alt         = altInput.text.toString().toDoubleOrNull()
            val ast         = astInput.text.toString().toDoubleOrNull()

            val anyFilled = listOf(hba1c, cpeptide, hemoglobin, leukocytes, platelets,
                cholesterol, hdl, ldl, triglyc, creatinine, urea, alt, ast).any { it != null }

            if (!anyFilled) {
                Toast.makeText(requireContext(), "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveAnalysis(
                type = type,
                hba1c = hba1c,
                cpeptide = cpeptide,
                hemoglobin = hemoglobin,
                leukocytes = leukocytes,
                platelets = platelets,
                cholesterol = cholesterol,
                hdl = hdl,
                ldl = ldl,
                triglycerides = triglyc,
                creatinine = creatinine,
                urea = urea,
                alt = alt,
                ast = ast
            )
        }

        return view
    }
}
