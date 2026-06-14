package dev.moorhen.diahelp.view.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButtonToggleGroup
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.viewmodel.ChartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChartFragment : Fragment() {

    private val viewModel: ChartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)

        val lineChart = view.findViewById<LineChart>(R.id.lineChart)
        val noDataText = view.findViewById<TextView>(R.id.tvNoChartData)
        val dropdown = view.findViewById<AutoCompleteTextView>(R.id.chartPeriodDropdown)
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.chartToggleGroup)

        setupChartAppearance(lineChart)

        val periods = listOf("1 День", "1 Неделя", "1 Месяц", "3 Месяца", "6 Месяцев", "1 Год")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, periods)
        dropdown.setAdapter(adapter)
        dropdown.setText(viewModel.selectedPeriod.value, false)

        dropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setPeriod(periods[position])
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnChartSugar -> viewModel.toggleMode(true)
                    R.id.btnChartInsulin -> viewModel.toggleMode(false)
                }
                updateChart(lineChart, noDataText)
            }
        }

        viewModel.sugarData.observe(viewLifecycleOwner) { updateChart(lineChart, noDataText) }
        viewModel.insulinData.observe(viewLifecycleOwner) { updateChart(lineChart, noDataText) }

        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    private fun setupChartAppearance(lineChart: LineChart) {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.axisRight.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            private val sdf = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                return sdf.format(Date(value.toLong()))
            }
        }
    }

    private fun updateChart(lineChart: LineChart, noDataText: TextView) {
        val isSugar = viewModel.isSugarMode.value ?: true
        val rawData = if (isSugar) viewModel.sugarData.value else viewModel.insulinData.value
        val data = rawData ?: emptyList()

        if (data.isEmpty()) {
            lineChart.clear()
            lineChart.visibility = View.INVISIBLE
            noDataText.visibility = View.VISIBLE
            return
        }

        lineChart.visibility = View.VISIBLE
        noDataText.visibility = View.GONE

        val entries = data.map { Entry(it.first.toFloat(), it.second.toFloat()) }

        val label = if (isSugar) "Уровень сахара (ммоль/л)" else "Доза инсулина (ед.)"
        val colorRes = if (isSugar) R.color.blueLow else R.color.greenNormal

        val dataSet = LineDataSet(entries, label).apply {
            color = androidx.core.content.ContextCompat.getColor(requireContext(), colorRes)
            setCircleColor(androidx.core.content.ContextCompat.getColor(requireContext(), colorRes))
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextColor = Color.BLACK
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }
}
