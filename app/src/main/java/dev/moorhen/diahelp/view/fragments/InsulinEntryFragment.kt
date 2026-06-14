// dev.moorhen.diahelp.view.fragments.InsulinEntryFragment
package dev.moorhen.diahelp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.utils.SessionManager
import dev.moorhen.diahelp.viewmodel.InsulinEntryViewModel
import dev.moorhen.diahelp.viewmodel.InsulinEntryViewModelFactory

class InsulinEntryFragment : Fragment() {

    private lateinit var viewModel: InsulinEntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_insulin_entry, container, false)

        val repository = InsulinRepository(requireContext())
        val session = SessionManager(requireContext())
        val factory = InsulinEntryViewModelFactory(repository, requireActivity().application, session)
        viewModel = ViewModelProvider(this, factory)[InsulinEntryViewModel::class.java]

        val insulinInput = view.findViewById<TextInputEditText>(R.id.inputInsulinDose)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        btnSave.setOnClickListener {
            viewModel.insulinDose = insulinInput.text.toString().toDoubleOrNull()

            val success = viewModel.saveNote()
            if (success) parentFragmentManager.popBackStack()
        }

        return view
    }
}