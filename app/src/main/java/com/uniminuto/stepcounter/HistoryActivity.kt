package com.uniminuto.stepcounter

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Mostrar título en la barra superior
        supportActionBar?.title = "Historial"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val barChart = findViewById<BarChart>(R.id.barChart)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val tvSummary = findViewById<TextView>(R.id.tvSummary)

        // Recuperar el historial desde StepStorage
        val history = StepStorage.getHistory(this)

        if (history.isEmpty()) {
            // Sin datos: mostrar mensaje y ocultar el gráfico
            barChart.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvSummary.visibility = View.GONE
            return
        }

        // Convertir el historial al formato que espera MPAndroidChart
        // BarEntry(x, y) donde x es el índice de la barra e y el valor
        val entries = history.mapIndexed { index, (_, steps) ->
            BarEntry(index.toFloat(), steps.toFloat())
        }

        // Etiquetas del eje X: solo mostramos MM-dd para no saturar
        val labels = history.map { (date, _) -> date.substring(5) }

        // Crear el dataset con estilo
        val dataSet = BarDataSet(entries, "Pasos por día").apply {
            color = 0xFF1976D2.toInt()  // Azul Material Design
            valueTextSize = 12f
            valueTextColor = 0xFF333333.toInt()
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.7f
        }

        // Configurar el gráfico
        barChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)
            setDrawGridBackground(false)
            setDrawBorders(false)

            // Configurar eje X (fechas)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                setDrawGridLines(false)
                textSize = 11f
            }

            // Eje Y derecho desactivado, solo usamos el izquierdo
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f

            // Animación de entrada
            animateY(800)

            // Refrescar
            invalidate()
        }

        // Mostrar resumen estadístico
        val total = history.sumOf { it.second }
        val average = total / history.size
        val max = history.maxOf { it.second }
        tvSummary.text = "Total: $total | Promedio: $average | Máximo: $max"
    }

    /**
     * Manejar el botón "atrás" de la barra superior para volver a MainActivity.
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}