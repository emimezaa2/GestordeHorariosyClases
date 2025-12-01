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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

/**
 * Pantalla que muestra la lista de clases guardadas.
 * Permite ver, editar y eliminar clases desde un RecyclerView.
 * También contiene el menú lateral con acceso a otras secciones de la app.
 */
class ListaClasesActivity : AppCompatActivity() {


    //  VARIABLES


    // Elementos visuales y objetos usados en la pantalla
    private lateinit var drawerLayout: DrawerLayout       // Controla el menú lateral
    private lateinit var navView: NavigationView          // Vista del menú lateral
    private lateinit var btnMenu: ImageButton             // Botón para abrir el menú
    private lateinit var recyclerView: RecyclerView       // Lista visual de clases
    private lateinit var adapter: ClaseAdapter            // Adaptador para mostrar las clases
    private lateinit var dbHelper: BaseDeDatosHelper      // Controlador de la base de datos
    private lateinit var prefs: SharedPreferences         // Preferencias para guardar datos del usuario


    //  MÉTODO ONCREATE


    /**
     * Se ejecuta al abrir la pantalla.
     * Configura la vista, el adaptador de clases, y el menú lateral.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_clases)

        // Inicializamos base de datos y preferencias
        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.rvListaClases)


        // Configuración del RecyclerView rejilla


        // Mostramos las clases en una cuadrícula de 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Adaptador con las funciones para editar y eliminar
        adapter = ClaseAdapter(
            listaClases = dbHelper.obtenerTodasLasClases(),
            onEliminarClick = { idClase -> mostrarConfirmacionBorrar(idClase) },
            onItemClick = { idClase ->
                // Abre la pantalla de edición de clase al tocar una tarjeta
                val intent = Intent(this, RegistroClasesActivity::class.java)
                intent.putExtra("ID_CLASE", idClase)
                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter

        // Configura el menú lateral (Navigation Drawer)
        configurarMenu()

        // Actualiza la información del usuario en el menú
        actualizarNavHeader()
    }


    // MÉTODO ONRESUME


    /**
     * Se ejecuta cada vez que la pantalla vuelve a mostrarse.
     * Refresca la lista de clases y los datos del perfil por si cambiaron.
     */
    override fun onResume() {
        super.onResume()

        // Si ya existe el adaptador, actualizamos la lista
        if (::adapter.isInitialized) {
            adapter.actualizarLista(dbHelper.obtenerTodasLasClases())
        }

        // También actualizamos el encabezado del menú lateral
        actualizarNavHeader()
    }


    // ACTUALIZAR NAV HEADER


    /**
     * Actualiza el nombre, carrera y foto del usuario en el menú lateral.
     */
    private fun actualizarNavHeader() {
        val navHeader = navView.getHeaderView(0)
        val headerImage = navHeader.findViewById<ImageView>(R.id.navHeaderImage)
        val headerName = navHeader.findViewById<TextView>(R.id.navHeaderName)
        val headerSubtitle = navHeader.findViewById<TextView>(R.id.navHeaderSubtitle)

        // Se obtienen los datos guardados en las preferencias
        val savedName = prefs.getString("user_name", "Gestor Academico")
        val savedCareer = prefs.getString("user_carrera", "Estudiante")
        val photoUriString = prefs.getString("user_photo_uri", null)

        headerName.text = savedName
        headerSubtitle.text = savedCareer

        // Si el usuario tiene una foto guardada, se muestra; de lo contrario, se pone la imagen por defecto
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


    //  CONFIRMAR ELIMINACION


    /**
     * Muestra un cuadro de diálogo para confirmar antes de eliminar una clase.
     * Si el usuario acepta, se borra la clase y sus tareas relacionadas.
     */
    private fun mostrarConfirmacionBorrar(idClase: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Clase")
            .setMessage("¿Seguro que quieres borrar esta clase? Se borraran tambien sus tareas.")
            .setPositiveButton("Si, borrar") { _, _ ->
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





    /**
     * Configura el menú lateral con las diferentes secciones de la app.
     * Cada opción abre una pantalla diferente.
     */
    private fun configurarMenu() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)

        // Abre el menú lateral al presionar el botón
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        // Acciones de cada opción del menú
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
