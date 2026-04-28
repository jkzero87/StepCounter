# 📱 StepCounter - Contador de Pasos Android

Aplicación Android nativa para contar pasos en tiempo real utilizando el sensor acelerómetro del dispositivo. Implementa un algoritmo de procesamiento de señal con filtro pasa-bajos y detección por umbral con histéresis (Schmitt trigger digital).

## 🎯 Características

- 📊 Lectura del acelerómetro en tiempo real (50 Hz)
- 🚶 Algoritmo propio de detección de pasos basado en procesamiento de señal
- 💾 Persistencia diaria del conteo con SharedPreferences
- 📈 Visualización del historial mediante gráfico de barras
- 🔄 Restauración automática del conteo al reabrir la app
- 🔋 Gestión eficiente de batería (sensor activo solo en primer plano)

## 🛠️ Tecnologías

- **Lenguaje:** Kotlin
- **SDK mínimo:** Android 7.0 (API 24)
- **SDK objetivo:** Android 14 (API 34)
- **Librerías:**
  - AndroidX (AppCompat, Core KTX)
  - MPAndroidChart v3.1.0 para gráficos
- **Sensor:** Sensor.TYPE_ACCELEROMETER

## 🧠 Algoritmo de detección

El algoritmo procesa las lecturas del acelerómetro en cuatro etapas:

1. **Cálculo de magnitud:** se obtiene el módulo del vector de aceleración 3D para hacer la detección invariante a la orientación del dispositivo.

2. **Filtro pasa-bajos IIR de primer orden** (α = 0.8): atenúa el ruido del sensor manteniendo la señal del paso (~1-2 Hz).

3. **Detección por Schmitt trigger:** dos umbrales (alto: 11.5 m/s², bajo: 10.0 m/s²) con histéresis para evitar conteos espurios.

4. **Debounce temporal:** intervalo mínimo de 250 ms entre pasos.

## 📋 Permisos requeridos

- ACTIVITY_RECOGNITION (Android 10+)
- Hardware del acelerómetro (uses-feature)

## 🚀 Instalación

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza Gradle (descarga MPAndroidChart desde JitPack)
4. Conecta un dispositivo físico con depuración USB activada
5. Ejecuta la app

## 📚 Referencias académicas

- Forrester, A. et al. (2021). How to build Android apps with Kotlin. Packt Publishing.
- Kumar S., A. (2018). Mastering Firebase for Android Development. Packt Publishing.

## 👤 Autor

Desarrollado como proyecto académico - Corporación Universitaria Minuto de Dios (UNIMINUTO)
