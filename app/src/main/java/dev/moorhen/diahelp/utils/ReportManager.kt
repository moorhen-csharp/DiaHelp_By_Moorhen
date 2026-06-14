package dev.moorhen.diahelp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.model.SugarModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Формирует отчёты (CSV и PDF) по записям сахара и инсулина пользователя.
 * Готовые файлы сохраняются во внутреннее хранилище приложения
 * (каталог "reports") и могут быть переданы пользователю через
 * FileProvider/Intent.ACTION_SEND из вызывающего кода.
 */
class ReportManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    private fun reportsDir(): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Экспортирует записи сахара и инсулина в CSV-файл.
     * Возвращает файл с отчётом.
     */
    fun exportCsv(
        sugarNotes: List<SugarModel>,
        insulinNotes: List<InsulinModel>,
        fileName: String = "diahelp_report.csv"
    ): File {
        val file = File(reportsDir(), fileName)

        FileOutputStream(file).use { fos ->
            val sb = StringBuilder()

            // Раздел: уровень сахара
            sb.append("Дата;Уровень сахара (ммоль/л);Время измерения;Самочувствие;Доза инсулина (ед.)\n")
            for (note in sugarNotes) {
                val sugar = if (note.SugarLevel == -1.0) "не измерял" else note.SugarLevel.toString()
                sb.append(dateFormat.format(note.Date)).append(';')
                    .append(sugar).append(';')
                    .append(note.MeasurementTime).append(';')
                    .append(note.HealthType).append(';')
                    .append(note.InsulinDose).append('\n')
            }

            sb.append('\n')

            // Раздел: инсулин
            sb.append("Дата;Доза инсулина (ед.)\n")
            for (note in insulinNotes) {
                sb.append(dateFormat.format(note.Date)).append(';')
                    .append(note.InsulinDose).append('\n')
            }

            // BOM для корректного отображения кириллицы в Excel
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)
            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    /**
     * Экспортирует записи сахара и инсулина в PDF-отчёт
     * со сводной статистикой и табличными данными.
     */
    fun exportPdf(
        sugarNotes: List<SugarModel>,
        insulinNotes: List<InsulinModel>,
        userName: String,
        fileName: String = "diahelp_report.pdf"
    ): File {
        val file = File(reportsDir(), fileName)

        val document = PdfDocument()
        val pageWidth = 595 // A4 при 72dpi
        val pageHeight = 842
        val margin = 40f

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isFakeBoldText = true
        }

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = page.canvas
        var y = margin

        canvas.drawText("Отчёт DiaHelp", margin, y, titlePaint)
        y += 24f
        canvas.drawText("Имя: $userName", margin, y, textPaint)
        y += 18f
        canvas.drawText("Дата формирования отчета: ${dateFormat.format(java.util.Date())}", margin, y, textPaint)
        y += 24f

        // Сводная статистика
        val validSugar = sugarNotes.filter { it.SugarLevel != -1.0 }
        val avgSugar = if (validSugar.isNotEmpty()) validSugar.map { it.SugarLevel }.average() else 0.0
        val avgInsulin = if (insulinNotes.isNotEmpty()) insulinNotes.map { it.InsulinDose }.average() else 0.0

        canvas.drawText("Сводная статистика", margin, y, headerPaint)
        y += 18f
        canvas.drawText("Записей сахара: ${sugarNotes.size}", margin, y, textPaint)
        y += 16f
        canvas.drawText("Средний уровень сахара: ${"%.1f".format(avgSugar)} ммоль/л", margin, y, textPaint)
        y += 16f
        canvas.drawText("Записей инсулина: ${insulinNotes.size}", margin, y, textPaint)
        y += 16f
        canvas.drawText("Средняя доза инсулина: ${"%.1f".format(avgInsulin)} ед.", margin, y, textPaint)
        y += 24f

        // Таблица: уровень сахара
        canvas.drawText("История измерений сахара", margin, y, headerPaint)
        y += 18f
        canvas.drawText("Дата", margin, y, headerPaint)
        canvas.drawText("Сахар", margin + 150f, y, headerPaint)
        canvas.drawText("Время", margin + 230f, y, headerPaint)
        canvas.drawText("Самочувствие", margin + 340f, y, headerPaint)
        canvas.drawText("Инсулин", margin + 460f, y, headerPaint)
        y += 16f

        for (note in sugarNotes) {
            if (y > pageHeight - margin) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = page.canvas
                y = margin
            }

            val sugarText = if (note.SugarLevel == -1.0) "—" else "%.1f".format(note.SugarLevel)
            canvas.drawText(dateFormat.format(note.Date), margin, y, textPaint)
            canvas.drawText(sugarText + "ммоль/л", margin + 150f, y, textPaint)
            canvas.drawText(note.MeasurementTime, margin + 230f, y, textPaint)
            canvas.drawText(note.HealthType, margin + 340f, y, textPaint)
            canvas.drawText("%.1f ед.".format(note.InsulinDose), margin + 460f, y, textPaint)
            y += 16f
        }

        y += 16f
        if (y > pageHeight - margin - 60f) {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            canvas = page.canvas
            y = margin
        }

        // Таблица: инсулин
        canvas.drawText("История введения инсулина", margin, y, headerPaint)
        y += 18f
        canvas.drawText("Дата", margin, y, headerPaint)
        canvas.drawText("Доза", margin + 200f, y, headerPaint)
        y += 16f

        for (note in insulinNotes) {
            if (y > pageHeight - margin) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = page.canvas
                y = margin
            }

            canvas.drawText(dateFormat.format(note.Date), margin, y, textPaint)
            canvas.drawText("%.1f ед.".format(note.InsulinDose), margin + 200f, y, textPaint)
            y += 16f
        }

        document.finishPage(page)

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }
}
