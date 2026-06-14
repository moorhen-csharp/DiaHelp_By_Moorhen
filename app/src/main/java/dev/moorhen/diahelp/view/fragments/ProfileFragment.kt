package dev.moorhen.diahelp.view.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.view.activity.AuthorizationActivity
import dev.moorhen.diahelp.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            Toast.makeText(
                requireContext(),
                "Без разрешения на уведомления напоминания работать не будут",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val userName = view.findViewById<TextView>(R.id.tvUserName)
        val userEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val logoutButton = view.findViewById<Button>(R.id.btnLogout)
        val themeSwitch = view.findViewById<Switch>(R.id.themeSwitch)
        val userCoeff = view.findViewById<TextView>(R.id.tvUserCoeffIns)
        val reminderSwitch = view.findViewById<Switch>(R.id.reminderSwitch)
        val tvReminderTime = view.findViewById<TextView>(R.id.tvReminderTime)
        val btnExportCsv = view.findViewById<MaterialButton>(R.id.btnExportCsv)
        val btnExportPdf = view.findViewById<MaterialButton>(R.id.btnExportPdf)

        // 👤 Данные пользователя
        userName.text = viewModel.getUserName()
        userEmail.text = viewModel.getUserEmail()
        userCoeff.text = "${viewModel.getUserCoeffInsulin()} ед."

        // 🎨 Тема
        val isDarkMode = viewModel.isDarkThemeEnabled(requireContext())
        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.parent.requestDisallowInterceptTouchEvent(true)
            }
            false
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveThemePreference(requireContext(), isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // 🔔 Напоминания
        reminderSwitch.isChecked = viewModel.isReminderEnabled()
        updateReminderTimeLabel(tvReminderTime)
        tvReminderTime.visibility = if (reminderSwitch.isChecked) View.VISIBLE else View.GONE

        reminderSwitch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.parent.requestDisallowInterceptTouchEvent(true)
            }
            false
        }

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            tvReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.setReminderEnabled(true)
                }
            } else {
                viewModel.setReminderEnabled(false)
            }
        }

        tvReminderTime.setOnClickListener {
            val hour = viewModel.getReminderHour()
            val minute = viewModel.getReminderMinute()

            TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    viewModel.setReminderTime(selectedHour, selectedMinute)
                    updateReminderTimeLabel(tvReminderTime)
                },
                hour,
                minute,
                true
            ).show()
        }

        // 📄 Экспорт отчётов
        btnExportCsv.setOnClickListener { viewModel.exportCsv() }
        btnExportPdf.setOnClickListener { viewModel.exportPdf() }

        viewModel.exportedFile.observe(viewLifecycleOwner) { file ->
            if (file != null) {
                shareFile(file)
                viewModel.clearExportedFile()
            }
        }

        viewModel.exportError.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearExportError()
            }
        }

        // 🚪 Выход
        logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти из профиля?")
                .setPositiveButton("Да") { _, _ -> viewModel.onLogoutClicked() }
                .setNegativeButton("Отмена", null)
                .show()
        }

        viewModel.logout.observe(viewLifecycleOwner) { shouldLogout ->
            if (shouldLogout) {
                startActivity(Intent(requireContext(), AuthorizationActivity::class.java))
                requireActivity().finish()
            }
        }

        return view
    }

    private fun updateReminderTimeLabel(textView: TextView) {
        val hour = viewModel.getReminderHour()
        val minute = viewModel.getReminderMinute()
        textView.text = "Время напоминания: %02d:%02d".format(hour, minute)
    }

    private fun shareFile(file: java.io.File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val mimeType = if (file.extension == "pdf") "application/pdf" else "text/csv"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Поделиться отчётом"))
    }
}
