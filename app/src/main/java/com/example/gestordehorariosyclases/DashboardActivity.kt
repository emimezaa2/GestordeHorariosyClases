package com.example.gestordehorariosyclases

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageButton
    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences // Preferencias del usuario (nombre, carrera, foto)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)

        configurarMenu()
        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
        actualizarNavHeader() // Refrescar nombre y foto en el menú lateral
    }

    // -------------------------------------------------------
    // ---------- ACTUALIZAR CABECERA DEL SIDEBAR ------------
    // -------------------------------------------------------
    /**
     * Actualiza la información del encabezado del menú lateral.
     * Muestra el nombre del usuario, carrera y foto de perfil.
     */
    private fun actualizarNavHeader() {
        val navHeader = navView.getHeaderView(0)
        val headerImage = navHeader.findViewById<ImageView>(R.id.navHeaderImage)
        val headerName = navHeader.findViewById<TextView>(R.id.navHeaderName)
        val headerSubtitle = navHeader.findViewById<TextView>(R.id.navHeaderSubtitle)

        val savedName = prefs.getString("user_name", "Gestor Académico")
        val savedCareer = prefs.getString("user_carrera", "Estudiante")
        val photoUriString = prefs.getString("user_photo_uri", null)

        headerName.text = savedName
        headerSubtitle.text = savedCareer

        if (photoUriString != null) {
            try {
                headerImage.setImageURI(Uri.parse(photoUriString))
            } catch (e: Exception) {
                headerImage.setImageResource(R.mipmap.ic_launcher_round)
            }
        } else {
            headerImage.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    // -------------------------------------------------------
    // ----------------- CARGAR DATOS EN PANTALLA ------------
    // -------------------------------------------------------
    /**
     * Carga todos los datos del Dashboard:
     * - Horas semanales de clase.
     * - Número de tareas pendientes.
     * - Próxima tarea y próxima clase.
     * - Avance semanal (progreso de la semana).
     */
    private fun cargarDatos() {
        // Datos del resumen
        val horasSemanales = dbHelper.calcularHorasSemanales()
        val totalPendientes = dbHelper.contarTareasPendientes()
        val proximaTarea = dbHelper.obtenerProximaTarea()

        // Enlazar vistas del layout
        val tvHoras = findViewById<TextView>(R.id.tvTotalClases)
        val tvPendientes = findViewById<TextView>(R.id.tvTareasPendientes)
        val tvProximaTarea = findViewById<TextView>(R.id.tvProximaTarea)
        val tvProximaClase = findViewById<TextView>(R.id.tvProximaClase)
        val progressCircular = findViewById<ProgressBar>(R.id.progressBarCircular)
        val tvProgresoTexto = findViewById<TextView>(R.id.tvProgresoTexto)
        val llContainer = findViewById<LinearLayout>(R.id.llDesgloseContainer)

        // Asignar valores obtenidos
        tvHoras.text = horasSemanales
        tvPendientes.text = totalPendientes.toString()
        tvProximaTarea.text = proximaTarea

        // Lista de tareas pendientes en el Dashboard
        val listaPendientes = dbHelper.obtenerTareasPendientesDetalladas()
        llContainer.removeAllViews()

        if (listaPendientes.isEmpty()) {
            // Si no hay tareas pendientes
            val tvVacio = TextView(this).apply {
                text = "¡Todo limpio! No tienes pendientes 🎉"
                setTextColor(getColor(android.R.color.darker_gray))
                textSize = 14f
            }
            llContainer.addView(tvVacio)
        } else {
            // Crear una tarjeta para cada tarea pendiente
            for (tarea in listaPendientes) {
                val view = LayoutInflater.from(this).inflate(R.layout.item_tarea, llContainer, false)

                val tvNombre = view.findViewById<TextView>(R.id.tvNombreTarea)
                val tvMateria = view.findViewById<TextView>(R.id.tvMateria)
                val tvFecha = view.findViewById<TextView>(R.id.tvFecha)
                val tvTipo = view.findViewById<TextView>(R.id.tvTipo)
                val viewColor = view.findViewById<View>(R.id.viewPrioridadColor)
                val cbCompletada = view.findViewById<CheckBox>(R.id.checkBoxFinal)
                val btnEliminar = view.findViewById<ImageButton>(R.id.btnEliminarTarea)

                // Ocultar botón de eliminar en el Dashboard
                btnEliminar.visibility = View.GONE

                // Asignar datos
                tvNombre.text = tarea.nombre
                tvMateria.text = tarea.nombreClase
                tvFecha.text = tarea.fecha
                tvTipo.text = tarea.tipo.uppercase()

                // Colores por prioridad
                val color = when (tarea.prioridad) {
                    "Alta" -> Color.parseColor("#F44336")
                    "Media" -> Color.parseColor("#FFC107")
                    else -> Color.parseColor("#4CAF50")
                }
                viewColor.setBackgroundColor(color)

                // Marcar tarea como completada desde el Dashboard
                cbCompletada.isChecked = false
                cbCompletada.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        dbHelper.actualizarEstadoTarea(tarea.id, true)
                        Toast.makeText(this, "¡Tarea completada!", Toast.LENGTH_SHORT).show()
                        cargarDatos() // Recargar la lista
                    }
                }
                llContainer.addView(view)
            }
        }

        // Mostrar próxima clase y progreso semanal
        val proximaClaseTexto = calcularProximaClase()
        tvProximaClase.text = proximaClaseTexto

        // Cálculo de avance de la semana (porcentaje)
        val calendario = Calendar.getInstance()
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
        val diaAjustado = if (diaSemana == Calendar.SUNDAY) 7 else diaSemana - 1
        val porcentaje = (diaAjustado * 100) / 7

        progressCircular.progress = porcentaje

        val nombreDia = when (diaAjustado) {
            1 -> "Lun"; 2 -> "Mar"; 3 -> "Mié"; 4 -> "Jue"
            5 -> "Vie"; 6 -> "Sáb"; 7 -> "Dom"; else -> ""
        }
        tvProgresoTexto.text = "$nombreDia\n$porcentaje%"
    }

    // -------------------------------------------------------
    // ----------------- CALCULAR PRÓXIMA CLASE --------------
    // -------------------------------------------------------
    /**
     * Busca cuál es la próxima clase del día actual.
     * Si ya pasaron todas las clases, muestra "Clases terminadas".
     */
    private fun calcularProximaClase(): String {
        val calendario = Calendar.getInstance()
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)

        val hoyString = when (diaSemana) {
            2 -> "Lun"; 3 -> "Mar"; 4 -> "Mié"; 5 -> "Jue"
            6 -> "Vie"; 7 -> "Sáb"; else -> "Dom"
        }

        val todasLasClases = dbHelper.obtenerTodasLasClases()
        val clasesDeHoy = todasLasClases.filter { it.dias.contains(hoyString) }

        if (clasesDeHoy.isEmpty()) return "Sin clases hoy 😴"

        val clasesOrdenadas = clasesDeHoy.sortedBy { it.horaInicio }
        val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendario.get(Calendar.MINUTE)
        val tiempoActual = horaActual * 60 + minutoActual

        for (clase in clasesOrdenadas) {
            try {
                val partes = clase.horaInicio.split(":")
                val h = partes[0].toInt()
                val m = partes[1].toInt()
                if ((h * 60 + m) > tiempoActual) {
                    return "${clase.nombre}\n${clase.salon} (${clase.horaInicio})"
                }
            } catch (_: Exception) { continue }
        }
        return "Clases terminadas por hoy"
    }

    // -------------------------------------------------------
    // ----------------- CONFIGURAR MENÚ LATERAL -------------
    // -------------------------------------------------------
    /**
     * Configura las acciones del menú lateral (Navigation Drawer).
     * Permite moverse entre módulos del proyecto.
     */
    private fun configurarMenu() {
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeScreenActivity::class.java))
                R.id.nav_registro_clase -> startActivity(Intent(this, RegistroClasesActivity::class.java))
                R.id.nav_ver_horario -> startActivity(Intent(this, ListaClasesActivity::class.java))
                R.id.nav_nueva_tarea -> startActivity(Intent(this, AgregarTareaActivity::class.java))
                R.id.nav_ver_tareas -> startActivity(Intent(this, ListaTareasActivity::class.java))
                R.id.nav_dashboard -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
            }
            finish()
            true
        }
    }
}
