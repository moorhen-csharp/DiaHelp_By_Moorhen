package dev.moorhen.diahelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import dev.moorhen.diahelp.R

class MedicalIndicationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_medical_indications, container, false)

        fun openAnalysis(type: String, title: String) {
            val fragment = MedicalAnalysisFragment.newInstance(type, title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
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
            openAnalysis("hormones", "Гормоны")
        }

        return view
    }
}
