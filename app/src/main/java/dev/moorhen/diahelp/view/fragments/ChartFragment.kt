package dev.moorhen.diahelp.view.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButtonToggleGroup
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.views.chart.ChartView
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.viewmodel.ChartViewModel

class ChartFragment : Fragment() {

    private val viewModel: ChartViewModel by viewModels()
    private val producer = ChartEntryModelProducer()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)

        val chartView   = view.findViewById<ChartView>(R.id.chartView)
        val noDataText  = view.findViewById<TextView>(R.id.tvNoChartData)
        val dropdown    = view.findViewById<AutoCompleteTextView>(R.id.chartPeriodDropdown)
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.chartToggleGroup)
        val maxValue = view.findViewById<TextView>(R.id.tvMaxValue)
        val minValue = view.findViewById<TextView>(R.id.tvMinValue)
        var avgValue = view.findViewById<TextView>(R.id.tvAvgValue)

        chartView.entryProducer = producer
        applyChartStyle(chartView)

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
                    R.id.btnChartSugar   -> viewModel.toggleMode(true)
                    R.id.btnChartInsulin -> viewModel.toggleMode(false)
                }
                updateChart(chartView, noDataText)
                applyChartStyle(chartView)
            }
        }

        viewModel.sugarData.observe(viewLifecycleOwner)   { updateChart(chartView, noDataText) }
        viewModel.insulinData.observe(viewLifecycleOwner) { updateChart(chartView, noDataText) }

        viewModel.average.observe(viewLifecycleOwner) { avg ->
                avgValue.text = String.format("%.1f ммоль/л", avg)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }


    /**
     * Задаём стиль графика программно через LineChart.LineSpec:
     * - цвет линии зависит от режима (сахар = синий, инсулин = зелёный)
     * - полупрозрачная заливка под линией
     * - круглые точки на каждом замере
     * - толщина линии 2.5dp
     */
    private fun applyChartStyle(chartView: ChartView) {
        val isSugar  = viewModel.isSugarMode.value ?: true
        val colorRes = if (isSugar) R.color.color_chart else R.color.color_chart
        val lineColor = ContextCompat.getColor(requireContext(), colorRes)

        // Заливка под линией — тот же цвет, но с alpha ~24%
        val fillColor = (lineColor and 0x00FFFFFF) or (60 shl 24)

        chartView.chart = LineChart(
            lines = listOf(
                LineChart.LineSpec(
                    lineColor          = lineColor,
                    lineThicknessDp    = 2.5f,
                    point              = ShapeComponent(
                        shape = Shapes.pillShape,
                        color = lineColor,
                    ),
                    pointSizeDp        = 8f,
                )
            )
        )
//        chartView.chart = ColumnChart(
//            columns = listOf(
//                LineComponent(
//                    color = lineColor,
//                    thicknessDp = 10f,
//                    shape = Shapes.pillShape,
//                )
//            )
//        )
    }

    private fun updateChart(chartView: ChartView, noDataText: TextView) {
        val isSugar = viewModel.isSugarMode.value ?: true
        val rawData = if (isSugar) viewModel.sugarData.value else viewModel.insulinData.value
        val data    = rawData ?: emptyList()

        if (data.isEmpty()) {
            chartView.visibility  = View.INVISIBLE
            noDataText.visibility = View.VISIBLE
            return
        }

        chartView.visibility  = View.VISIBLE
        noDataText.visibility = View.GONE

        val entries = data.mapIndexed { index, pair ->
            FloatEntry(x = index.toFloat(), y = pair.second.toFloat())
        }

        producer.setEntries(entries)
    }

}
