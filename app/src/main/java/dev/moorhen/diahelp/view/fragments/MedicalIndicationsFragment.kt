package dev.moorhen.diahelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.viewmodel.MedicalIndicationsViewModel

class MedicalIndicationsFragment : Fragment() {

    private lateinit var viewModel: MedicalIndicationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_medical_indications, container, false)
        viewModel = ViewModelProvider(this)[MedicalIndicationsViewModel::class.java]

        fun openAnalysis(type: String, title: String) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MedicalAnalysisFragment.newInstance(type, title))
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialCardView>(R.id.bloodAnalysis).setOnClickListener {
            openAnalysis("blood", "Анализ крови")
        }
        view.findViewById<MaterialCardView>(R.id.lipidsAnalysis).setOnClickListener {
            openAnalysis("lipids", "Липидный профиль")
        }
        view.findViewById<MaterialCardView>(R.id.biochemAnalysis).setOnClickListener {
            openAnalysis("biochem", "Биохимия")
        }
        view.findViewById<MaterialCardView>(R.id.hormonesAnalysis).setOnClickListener {
            // Раздел пока недоступен
        }

        // Загружаем последние значения для превью на карточках
        val tvBlood  = view.findViewById<TextView>(R.id.tvBloodLastValue)
        val tvLipids = view.findViewById<TextView>(R.id.tvLipidsLastValue)
        val tvBiochem = view.findViewById<TextView>(R.id.tvBiochemLastValue)

        viewModel.loadLatest("blood")
        viewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            if (record != null && record.analysisType == "blood") {
                val hba1c = record.hba1c
                if (hba1c != null) {
                    tvBlood.text = "HbA1c: $hba1c%  •  ${record.date.let {
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("ru")).format(it)
                    }}"
                }
            }
        }

        viewModel.loadLatest("lipids")
        viewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            if (record != null && record.analysisType == "lipids") {
                val chol = record.cholesterol
                if (chol != null) {
                    tvLipids.text = "Холестерин: $chol ммоль/л  •  ${record.date.let {
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("ru")).format(it)
                    }}"
                }
            }
        }

        viewModel.loadLatest("biochem")
        viewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            if (record != null && record.analysisType == "biochem") {
                val creat = record.creatinine
                if (creat != null) {
                    tvBiochem.text = "Креатинин: $creat мкмоль/л  •  ${record.date.let {
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("ru")).format(it)
                    }}"
                }
            }
        }

        return view
    }
}
