// dev.moorhen.diahelp.view.fragments.SugarNoteFragment
package dev.moorhen.diahelp.view.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.data.repository.InsulinRepository
import dev.moorhen.diahelp.data.repository.SugarRepository
import dev.moorhen.diahelp.utils.SessionManager
import dev.moorhen.diahelp.view.adapters.InsulinAdapter
import dev.moorhen.diahelp.view.adapters.SugarAdapter
import dev.moorhen.diahelp.viewmodel.SugarNoteViewModel
import dev.moorhen.diahelp.viewmodel.SugarNoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SugarNoteFragment : Fragment() {

    private lateinit var viewModel: SugarNoteViewModel
    private lateinit var sugarAdapter: SugarAdapter
    private lateinit var insulinAdapter: InsulinAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sugarnote, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerReadings)
        val btnAddData = view.findViewById<ImageButton>(R.id.btnAddData)
        val btnClear = view.findViewById<MaterialButton>(R.id.btnClear)
        val dropdown = view.findViewById<AutoCompleteTextView>(R.id.periodDropdown)
        val avgText = view.findViewById<TextView>(R.id.textAverage)
        val noDataText = view.findViewById<TextView>(R.id.tvNoData)
        val streakText = view.findViewById<TextView>(R.id.textStreak)
        val session = SessionManager(requireContext())
        val streakIcon = view.findViewById<ImageView>(R.id.sugarStreak)
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val btnFood = view.findViewById<MaterialButton>(R.id.btnFood)

        // Меняем текст кнопки "Еда" на "Инсулин"
        btnFood.text = "Инсулин"

        val streak = session.getStreak()
        updateStreakUI(streak, streakText, streakIcon)

        val sugarRepository = SugarRepository(requireContext())
        val insulinRepository = InsulinRepository(requireContext())
        val factory = SugarNoteViewModelFactory(sugarRepository, insulinRepository, requireActivity().application, session)
        viewModel = ViewModelProvider(this, factory)[SugarNoteViewModel::class.java]

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            noDataText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }

        sugarAdapter = SugarAdapter(emptyList())
        insulinAdapter = InsulinAdapter(emptyList())

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = sugarAdapter

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnSugar -> {
                        viewModel.toggleToSugar()
                        recycler.adapter = sugarAdapter
                        view.findViewById<TextView>(R.id.textAvgLabel).text = "Средний уровень сахара в крови:"
                    }
                    R.id.btnFood -> {
                        viewModel.toggleToInsulin()
                        recycler.adapter = insulinAdapter
                        view.findViewById<TextView>(R.id.textAvgLabel).text = "Среднее количество инсулина в день:"
                    }
                }
            }
        }

        viewModel.sugarNotes.observe(viewLifecycleOwner) {
            sugarAdapter.updateData(it)
        }

        viewModel.insulinNotes.observe(viewLifecycleOwner) {
            insulinAdapter.updateData(it)
        }

        viewModel.average.observe(viewLifecycleOwner) { avg ->
            if (viewModel.isSugarListVisible.value == true) {
                avgText.text = String.format("%.1f ммоль/л", avg)
            } else {
                avgText.text = String.format("%.1f ед", avg)
            }
        }

        val periods = listOf("1 День", "1 Неделя", "1 Месяц", "3 Месяца", "6 Месяцев", "1 Год")
        val adapterDropdown = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, periods)
        dropdown.setAdapter(adapterDropdown)

        dropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = periods[position]
            viewModel.selectedPeriod.value = selected
            viewModel.onPeriodChanged()
        }

        // Кнопка очистки (полный код)
        btnClear.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_clear_list, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.shape_dialog_containers)
            )

            dialog.show()

            val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnOk.setOnClickListener {
                viewModel.clearNotes()
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        btnAddData.setOnClickListener {
            if (viewModel.isSugarListVisible.value == true) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SugarEntryFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, InsulinEntryFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        viewModel.loadSugarNotes()
        viewModel.loadInsulinNotes()
        viewModel.onPeriodChanged()

        checkDailySugarDialog(session, streakText, streakIcon)

        return view
    }

    // ✅ Добавлен недостающий метод updateStreakUI
    private fun updateStreakUI(
        streak: Int,
        streakText: TextView,
        streakIcon: ImageView
    ) {
        streakText.text = streak.toString()

        val iconRes = if (streak > 0) {
            R.drawable.ic_sugar_streak_red
        } else {
            R.drawable.ic_sugar_streak_gray
        }

        streakIcon.setImageResource(iconRes)
    }

    // ✅ Добавлен недостающий метод checkDailySugarDialog
    private fun checkDailySugarDialog(
        session: SessionManager,
        streakText: TextView,
        streakIcon: ImageView
    ) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (session.getLastAskDate() == today) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_sugar_streak, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.shape_dialog_containers)
        )

        dialog.show()

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnOk.setOnClickListener {
            session.saveStreak(0)
            session.saveLastAskDate(today)
            updateStreakUI(0, streakText, streakIcon)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            val newStreak = session.getStreak() + 1
            session.saveStreak(newStreak)
            session.saveLastAskDate(today)
            updateStreakUI(newStreak, streakText, streakIcon)
            dialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isSugarListVisible.value == true) {
            viewModel.loadSugarNotes()
        } else {
            viewModel.loadInsulinNotes()
        }
        viewModel.onPeriodChanged()
    }
}