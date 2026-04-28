package com.uniminuto.stepcounter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt
import android.content.Intent

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val TAG = "StepCounter"

        // === Parámetros del algoritmo ===
        private const val FILTER_ALPHA = 0.8f
        private const val THRESHOLD_HIGH = 11.5f
        private const val THRESHOLD_LOW = 10.0f
        private const val MIN_STEP_INTERVAL_MS = 250L
    }

    // Componentes del framework de sensores
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Vistas de la interfaz
    private lateinit var tvStepCount: TextView
    private lateinit var tvMagnitude: TextView
    private lateinit var tvFiltered: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnReset: Button

    private lateinit var btnHistory: Button

    // Estado del contador
    private var stepCount: Int = 0

    // Estado del filtro
    private var filteredMagnitude: Float = 9.81f

    // Estado del Schmitt trigger
    private var armed: Boolean = false
    private var lastStepTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vincular vistas
        tvStepCount = findViewById(R.id.tvStepCount)
        tvMagnitude = findViewById(R.id.tvMagnitude)
        tvFiltered = findViewById(R.id.tvFiltered)
        tvStatus = findViewById(R.id.tvStatus)
        btnReset = findViewById(R.id.btnReset)

        btnHistory = findViewById(R.id.btnHistory)
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        btnReset.setOnClickListener {
            stepCount = 0
            StepStorage.saveTodaySteps(this, stepCount)
            updateStepCountUI()
            Toast.makeText(this, "Contador reiniciado", Toast.LENGTH_SHORT).show()
        }

        // Restaurar conteo del día actual desde almacenamiento
        stepCount = StepStorage.getTodaySteps(this)
        updateStepCountUI()
        // Inicializar sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            Toast.makeText(this, "Este dispositivo no tiene acelerómetro", Toast.LENGTH_LONG).show()
            tvStatus.text = "Estado: sin acelerómetro"
        } else {
            Log.d(TAG, "Sensor: ${accelerometer?.name}, " +
                    "Vendor: ${accelerometer?.vendor}, " +
                    "Resolución: ${accelerometer?.resolution} m/s², " +
                    "Rango máximo: ${accelerometer?.maximumRange} m/s², " +
                    "Consumo: ${accelerometer?.power} mA")
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            tvStatus.text = "Estado: leyendo sensor..."
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        // Guardar el conteo actual antes de salir
        StepStorage.saveTodaySteps(this, stepCount)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // 1) Calcular magnitud del vector
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        // 2) Filtro pasa-bajos IIR
        filteredMagnitude = FILTER_ALPHA * filteredMagnitude + (1 - FILTER_ALPHA) * magnitude

        // 3) Schmitt trigger con debounce
        val now = System.currentTimeMillis()

        if (!armed && filteredMagnitude > THRESHOLD_HIGH) {
            if (now - lastStepTimestamp > MIN_STEP_INTERVAL_MS) {
                stepCount++
                lastStepTimestamp = now
                armed = true
                updateStepCountUI()

                // Guardar cada 5 pasos para no escribir en disco a cada paso
                if (stepCount % 5 == 0) {
                    StepStorage.saveTodaySteps(this, stepCount)
                }

                Log.d(TAG, "PASO #$stepCount detectado | magnitud=%.2f | filtrada=%.2f"
                    .format(magnitude, filteredMagnitude))
            }
        } else if (armed && filteredMagnitude < THRESHOLD_LOW) {
            armed = false
        }

        // 4) Actualizar diagnóstico en pantalla
        tvMagnitude.text = "Magnitud cruda: %.2f".format(magnitude)
        tvFiltered.text = "Filtrada: %.2f".format(filteredMagnitude)
        tvStatus.text = if (armed) "Estado: pico activo ▲" else "Estado: esperando ▼"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No requiere acción
    }

    private fun updateStepCountUI() {
        tvStepCount.text = stepCount.toString()
    }
}