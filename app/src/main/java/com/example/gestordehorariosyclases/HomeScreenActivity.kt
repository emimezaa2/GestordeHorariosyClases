package com.example.gestordehorariosyclases

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import java.util.Calendar
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.ImageButton

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences

    // Componentes gui
    private lateinit var tvSaludo: TextView
    private lateinit var tvNombreUsuario: TextView
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Inicializar vistas
        tvSaludo = findViewById(R.id.tvSaludo)
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btnMenu)

        configurarMenu()
        configurarSaludo()
        cargarDatosTarjetas()     //  estadísticas principales

        // Botón que manda al dash
        val btnIrADashboard = findViewById<Button>(R.id.btnIrADashboard)
        btnIrADashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // a volver  se actualiza el saludo y el menú
        actualizarNavHeader()
        configurarSaludo()
    }


    /**
     * Muestra un saludo dinámico según la hora del día
     * y coloca el nombre del usuario guardado
     */
    private fun configurarSaludo() {
        val nombreGuardado = prefs.getString("user_name", "¡Estudiante!")
        tvNombreUsuario.text = nombreGuardado

        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val saludo = when (hora) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        tvSaludo.text = saludo
    }


    /**
     * Carga la información principal que se muestra en las tarjetas:
     *  Clases programadas para hoy.
     *  Número total de tareas pendientes.
     */
    private fun cargarDatosTarjetas() {
        val clasesHoy = dbHelper.contarClasesHoy()
        val pendientesTotal = dbHelper.contarTareasPendientes()

        findViewById<TextView>(R.id.tvClasesHoyCount).text = clasesHoy.toString()
        findViewById<TextView>(R.id.tvPendientesTotalCount).text = pendientesTotal.toString()
    }


    /**
     * actualiza los datos del encabezado del menú lateral
     *  Nombre del usuario
     * Carrera o rol
     * Foto de perfil
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


    /**
     * Define las acciones del menú lateral (Navigation Drawer).
     * Cada opción redirige a un módulo diferente de la aplicación.
     */
    private fun configurarMenu() {
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_registro_clase -> startActivity(Intent(this, RegistroClasesActivity::class.java))
                R.id.nav_ver_horario -> startActivity(Intent(this, ListaClasesActivity::class.java))
                R.id.nav_nueva_tarea -> startActivity(Intent(this, AgregarTareaActivity::class.java))
                R.id.nav_ver_tareas -> startActivity(Intent(this, ListaTareasActivity::class.java))
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
            }
            finish()
            true
        }
    }
}
