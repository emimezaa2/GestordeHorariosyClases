package com.example.gestordehorariosyclases

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class ListaClasesActivity : AppCompatActivity() {

    // -------------------------------------------------------
    // ------------------ VARIABLES GLOBALES ------------------
    // -------------------------------------------------------
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClaseAdapter
    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences

    // -------------------------------------------------------
    // -------------------- MÉTODO ONCREATE -------------------
    // -------------------------------------------------------
    /**
     * Inicializa la vista principal, configura el RecyclerView,
     * carga las clases registradas y activa la navegación lateral.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_clases)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Configurar RecyclerView con diseño vertical
        recyclerView = findViewById(R.id.rvListaClases)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adaptador con lógica de editar y eliminar clases
        adapter = ClaseAdapter(
            listaClases = dbHelper.obtenerTodasLasClases(),
            onEliminarClick = { idClase -> mostrarConfirmacionBorrar(idClase) },
            onItemClick = { idClase ->
                // Abrir pantalla de edición de clase
                val intent = Intent(this, RegistroClasesActivity::class.java)
                intent.putExtra("ID_CLASE", idClase)
                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter

        // Configuración del menú lateral y datos del perfil
        configurarMenu()
        actualizarNavHeader()
    }

    // -------------------------------------------------------
    // ------------------ MÉTODO ONRESUME ---------------------
    // -------------------------------------------------------
    /**
     * Refresca la lista de clases y los datos del menú lateral
     * cada vez que el usuario vuelve a esta pantalla.
     */
    override fun onResume() {
        super.onResume()

        // Refrescar lista (por si se modificó una clase)
        if (::adapter.isInitialized) {
            adapter.actualizarLista(dbHelper.obtenerTodasLasClases())
        }

        // Refrescar datos del usuario (nombre y foto)
        actualizarNavHeader()
    }

    // -------------------------------------------------------
    // ---------------- ACTUALIZAR NAV HEADER -----------------
    // -------------------------------------------------------
    /**
     * Carga la información del encabezado del menú lateral:
     * - Foto de perfil
     * - Nombre del usuario
     * - Carrera o rol
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
    // ---------------- CONFIRMAR ELIMINACIÓN ----------------
    // -------------------------------------------------------
    /**
     * Muestra una alerta de confirmación antes de eliminar una clase.
     * Si el usuario confirma, también se borran las tareas relacionadas.
     */
    private fun mostrarConfirmacionBorrar(idClase: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Clase")
            .setMessage("¿Seguro que quieres borrar esta clase? Se borrarán también sus tareas.")
            .setPositiveButton("Sí, borrar") { _, _ ->
                if (dbHelper.eliminarClase(idClase)) {
                    Toast.makeText(this, "Clase eliminada", Toast.LENGTH_SHORT).show()
                    adapter.actualizarLista(dbHelper.obtenerTodasLasClases())
                } else {
                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // -------------------------------------------------------
    // ------------------ CONFIGURAR MENÚ ---------------------
    // -------------------------------------------------------
    /**
     * Configura las opciones del menú lateral (Navigation Drawer)
     * para navegar entre las distintas pantallas del sistema.
     */
    private fun configurarMenu() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)

        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeScreenActivity::class.java))
                    finish()
                }
                R.id.nav_registro_clase -> {
                    startActivity(Intent(this, RegistroClasesActivity::class.java))
                    finish()
                }
                R.id.nav_ver_horario -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_nueva_tarea -> {
                    startActivity(Intent(this, AgregarTareaActivity::class.java))
                    finish()
                }
                R.id.nav_ver_tareas -> {
                    startActivity(Intent(this, ListaTareasActivity::class.java))
                    finish()
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish()
                }
            }
            true
        }
    }
}
