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
import android.widget.ProgressBar
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
import dev.moorhen.diahelp.utils.HealthConnectManager
import dev.moorhen.diahelp.view.activity.AuthorizationActivity
import dev.moorhen.diahelp.viewmodel.HcState
import dev.moorhen.diahelp.viewmodel.HealthConnectViewModel
import dev.moorhen.diahelp.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private val hcViewModel: HealthConnectViewModel by viewModels()

    // Запрос разрешения на уведомления
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setReminderEnabled(true)
        else Toast.makeText(
            requireContext(),
            "Без разрешения на уведомления напоминания работать не будут",
            Toast.LENGTH_LONG
        ).show()
    }

    // Запрос разрешений Health Connect
    private val requestHcPermissions = registerForActivityResult(
        HealthConnectManager.createPermissionRequestContract()
    ) { granted ->
        if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
            hcViewModel.checkStatus()
        } else {
            Toast.makeText(
                requireContext(),
                "Предоставьте все разрешения Health Connect для синхронизации данных",
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

        // ─── Базовые вью ───────────────────────────────────────
        val tvUserName    = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail   = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvUserCoeff   = view.findViewById<TextView>(R.id.tvUserCoeffIns)
        val btnLogout     = view.findViewById<Button>(R.id.btnLogout)
        val themeSwitch   = view.findViewById<Switch>(R.id.themeSwitch)
        val reminderSwitch= view.findViewById<Switch>(R.id.reminderSwitch)
        val tvReminderTime= view.findViewById<TextView>(R.id.tvReminderTime)
        val btnExportCsv  = view.findViewById<MaterialButton>(R.id.btnExportCsv)
        val btnExportPdf  = view.findViewById<MaterialButton>(R.id.btnExportPdf)

        // ─── Health Connect вью ────────────────────────────────
        val tvHcStatus  = view.findViewById<TextView>(R.id.tvHcStatus)
        val btnHcExport = view.findViewById<MaterialButton>(R.id.btnHcExport)
        val btnHcImport = view.findViewById<MaterialButton>(R.id.btnHcImport)
        val pbHc        = view.findViewById<ProgressBar>(R.id.pbHc)

        // ═══════════════════════════════════════════════════════
        // 👤 Данные пользователя
        // ═══════════════════════════════════════════════════════
        tvUserName.text  = viewModel.getUserName()
        tvUserEmail.text = viewModel.getUserEmail()
        tvUserCoeff.text = "${viewModel.getUserCoeffInsulin()} ед."

        // ═══════════════════════════════════════════════════════
        // 🎨 Тема
        // ═══════════════════════════════════════════════════════
        themeSwitch.isChecked = viewModel.isDarkThemeEnabled(requireContext())
        themeSwitch.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveThemePreference(requireContext(), isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // ═══════════════════════════════════════════════════════
        // 🔔 Напоминания
        // ═══════════════════════════════════════════════════════
        reminderSwitch.isChecked = viewModel.isReminderEnabled()
        updateReminderTimeLabel(tvReminderTime)
        tvReminderTime.visibility = if (reminderSwitch.isChecked) View.VISIBLE else View.GONE

        reminderSwitch.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            tvReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
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
            TimePickerDialog(
                requireContext(),
                { _, h, m -> viewModel.setReminderTime(h, m); updateReminderTimeLabel(tvReminderTime) },
                viewModel.getReminderHour(), viewModel.getReminderMinute(), true
            ).show()
        }

        // ═══════════════════════════════════════════════════════
        // 📄 Отчёты
        // ═══════════════════════════════════════════════════════
        btnExportCsv.setOnClickListener { viewModel.exportCsv() }
        btnExportPdf.setOnClickListener { viewModel.exportPdf() }

        viewModel.exportedFile.observe(viewLifecycleOwner) { file ->
            if (file != null) { shareFile(file); viewModel.clearExportedFile() }
        }
        viewModel.exportError.observe(viewLifecycleOwner) { msg ->
            if (msg != null) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); viewModel.clearExportError() }
        }

        // ═══════════════════════════════════════════════════════
        // 🏥 Health Connect
        // ═══════════════════════════════════════════════════════
        hcViewModel.checkStatus()

        hcViewModel.hcState.observe(viewLifecycleOwner) { state ->
            pbHc.visibility = View.GONE
            when (state) {
                is HcState.NotAvailable -> {
                    tvHcStatus.text = "❌ Health Connect не установлен на этом устройстве"
                    btnHcExport.isEnabled = false
                    btnHcImport.isEnabled = false
                }
                is HcState.NeedsPermissions -> {
                    tvHcStatus.text = "⚠️ Нужны разрешения Health Connect — нажмите кнопку ниже"
                    btnHcExport.isEnabled = true
                    btnHcImport.isEnabled = true
                    btnHcExport.text = "Дать разрешения"
                    btnHcImport.text = "Дать разрешения"
                    btnHcExport.setOnClickListener {
                        requestHcPermissions.launch(HealthConnectManager.PERMISSIONS)
                    }
                    btnHcImport.setOnClickListener {
                        requestHcPermissions.launch(HealthConnectManager.PERMISSIONS)
                    }
                }
                is HcState.Idle -> {
                    tvHcStatus.text = "✅ Health Connect подключён и готов к работе"
                    btnHcExport.isEnabled = true
                    btnHcImport.isEnabled = true
                    btnHcExport.text = "Экспорт в HC"
                    btnHcImport.text = "Импорт из HC"
                    btnHcExport.setOnClickListener { hcViewModel.exportToHealthConnect() }
                    btnHcImport.setOnClickListener { hcViewModel.importFromHealthConnect() }
                }
                is HcState.Loading -> {
                    tvHcStatus.text = "⏳ Синхронизация…"
                    pbHc.visibility = View.VISIBLE
                    btnHcExport.isEnabled = false
                    btnHcImport.isEnabled = false
                }
                is HcState.Success -> {
                    tvHcStatus.text = "✅ ${state.message}"
                    btnHcExport.isEnabled = true
                    btnHcImport.isEnabled = true
                    btnHcExport.text = "Экспорт в HC"
                    btnHcImport.text = "Импорт из HC"
                    btnHcExport.setOnClickListener { hcViewModel.exportToHealthConnect() }
                    btnHcImport.setOnClickListener { hcViewModel.importFromHealthConnect() }
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    hcViewModel.resetState()
                }
                is HcState.Error -> {
                    tvHcStatus.text = "❌ ${state.message}"
                    btnHcExport.isEnabled = true
                    btnHcImport.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    hcViewModel.resetState()
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // 🚪 Выход
        // ═══════════════════════════════════════════════════════
        btnLogout.setOnClickListener {
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

    private fun updateReminderTimeLabel(tv: TextView) {
        tv.text = "Время напоминания: %02d:%02d".format(
            viewModel.getReminderHour(), viewModel.getReminderMinute()
        )
    }

    private fun shareFile(file: java.io.File) {
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", file
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
