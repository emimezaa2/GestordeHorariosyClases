package com.example.gestordehorariosyclases

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class ListaTareasActivity : AppCompatActivity() {


    //  VARIABLES

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageButton
    private lateinit var tvSinTareas: TextView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TareaAdapter
    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences


    /**
     * Inicializa la pantalla de lista de tareas:
     * - Configura el RecyclerView y el adaptador.
     * - Carga las tareas guardadas.
     * - Configura el menú lateral y el botón flotante.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_tareas)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.rvListaTareas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        tvSinTareas = findViewById(R.id.tvSinTareas)
        fabAgregar = findViewById(R.id.fabAgregarTarea)

        // Configuración del adaptador con acciones personalizadas
        adapter = TareaAdapter(
            listaTareas = ArrayList(),
            onEliminarClick = { idTarea -> confirmarEliminar(idTarea) },
            onItemClick = { idTarea ->
                // Abre la pantalla para editar la tarea seleccionada
                val intent = Intent(this, AgregarTareaActivity::class.java)
                intent.putExtra("ID_TAREA", idTarea)
                startActivity(intent)
            },
            onCheckClick = { idTarea, estaCompletada ->
                dbHelper.actualizarEstadoTarea(idTarea, estaCompletada)
                val mensaje = if (estaCompletada) "¡Completada!" else "Pendiente"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        // Botón flotante para agregar una nueva tarea
        fabAgregar.setOnClickListener {
            startActivity(Intent(this, AgregarTareaActivity::class.java))
        }

        configurarMenu()
        cargarLista()
        actualizarNavHeader()
    }


    /**
     * Se ejecuta al volver a la actividad.
     * Refresca la lista de tareas y la cabecera del menú lateral.
     */
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            cargarLista()
        }
        actualizarNavHeader()
    }


    //  actualiza la brra lateral

    /**
     * Muestra en el menú lateral la información del usuari
     * Nombre completo
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
     * Obtiene todas las tareas desde la base de datos
     * y actualiza el adaptador. Muestra un mensaje si no hay tareas.
     */
    private fun cargarLista() {
        val lista = dbHelper.obtenerTodasLasTareas()
        adapter.actualizarLista(lista)

        // Mostrar mensaje si la lista está vacía
        if (lista.isEmpty()) {
            tvSinTareas.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvSinTareas.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }


    /**
     * Muestra un cuadro de confirmación antes de eliminar una tarea.
     * Si el usuario confirma, la tarea se borra y se actualiza la lista.
     */
    private fun confirmarEliminar(idTarea: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Tarea")
            .setMessage("¿Borrar esta actividad?")
            .setPositiveButton("Borrar") { _, _ ->
                if (dbHelper.eliminarTarea(idTarea)) {
                    Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    cargarLista()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    /**
     * Configura las opciones del menú lateral
     * para navegar entre pantallas
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
                R.id.nav_ver_horario -> {
                    startActivity(Intent(this, ListaClasesActivity::class.java))
                    finish()
                }
                R.id.nav_nueva_tarea -> {
                    startActivity(Intent(this, AgregarTareaActivity::class.java))
                    finish()
                }
                R.id.nav_ver_tareas -> drawerLayout.closeDrawer(GravityCompat.START)
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
