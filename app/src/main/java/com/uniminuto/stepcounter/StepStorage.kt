package com.uniminuto.stepcounter

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manejo de la persistencia del conteo diario de pasos.
 *
 * Estructura del SharedPreferences:
 *   - "yyyy-MM-dd" → Int (pasos de ese día)
 *   - "history_keys" → Set<String> (todas las fechas registradas)
 *
 * Es un objeto (singleton) porque no necesitamos múltiples instancias.
 */
object StepStorage {

    private const val PREFS_NAME = "step_counter_prefs"
    private const val KEY_HISTORY = "history_keys"

    /**
     * Devuelve la fecha actual en formato yyyy-MM-dd.
     * Este formato es ordenable lexicográficamente, lo cual nos ahorra parseo después.
     */
    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Recupera los pasos guardados del día actual.
     * Si no hay registro, devuelve 0.
     */
    fun getTodaySteps(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(todayKey(), 0)
    }

    /**
     * Guarda los pasos del día actual y registra la fecha en el set de historial.
     */
    fun saveTodaySteps(context: Context, steps: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = todayKey()

        // Recuperamos el set actual de fechas y le agregamos la de hoy
        // (los Set en SharedPreferences son inmutables, así que copiamos)
        val keys = prefs.getStringSet(KEY_HISTORY, mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()
        keys.add(key)

        prefs.edit()
            .putInt(key, steps)
            .putStringSet(KEY_HISTORY, keys)
            .apply()  // apply() es asíncrono, commit() es síncrono. apply() es preferido.
    }

    /**
     * Devuelve el historial completo ordenado por fecha ascendente.
     * Útil para mostrar gráficos.
     */
    fun getHistory(context: Context): List<Pair<String, Int>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keys = prefs.getStringSet(KEY_HISTORY, emptySet()) ?: emptySet()
        return keys.sorted().map { date -> date to prefs.getInt(date, 0) }
    }

    /**
     * Borra todo el historial. Útil para testing o un futuro botón "borrar datos".
     */
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}