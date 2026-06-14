// dev.moorhen.diahelp.view.fragments.InsulinListFragment
package dev.moorhen.diahelp.view.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.utils.SessionManager
import dev.moorhen.diahelp.view.adapters.InsulinAdapter
import dev.moorhen.diahelp.viewmodel.InsulinListViewModel
import dev.moorhen.diahelp.viewmodel.InsulinListViewModelFactory

class InsulinListFragment : Fragment() {

    private lateinit var viewModel: InsulinListViewModel
    private lateinit var adapter: InsulinAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_insulin_list, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerInsulinReadings)
        val btnAddData = view.findViewById<ImageButton>(R.id.btnAddInsulinData)
        val btnClear = view.findViewById<MaterialButton>(R.id.btnInsulinClear)
        val avgText = view.findViewById<TextView>(R.id.textInsulinAverage)

        val session = SessionManager(requireContext())
        val repository = InsulinRepository(requireContext())
        val factory = InsulinListViewModelFactory(repository, requireActivity().application, session)
        viewModel = ViewModelProvider(this, factory)[InsulinListViewModel::class.java]

        adapter = InsulinAdapter(emptyList())
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewModel.insulinNotes.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        viewModel.average.observe(viewLifecycleOwner) { avg ->
            avgText.text = String.format("%.1f ед", avg)
        }

        // Кнопка очистки
        btnClear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Очистить записи?")
                .setMessage("Вы уверены, что хотите удалить все записи об инсулине?")
                .setPositiveButton("Да") { _, _ -> viewModel.clearNotes() }
                .setNegativeButton("Нет", null)
                .show()
        }

        // Кнопка добавления
        btnAddData.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InsulinEntryFragment())
                .addToBackStack(null)
                .commit()
        }

        // Все данные загружаются автоматически в init ViewModel
        return view
    }

    override fun onResume() {
        super.onResume()
        // При возврате на экран просто перезагружаем все данные
        viewModel.loadAllData()
    }
}