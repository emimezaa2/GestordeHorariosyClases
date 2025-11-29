package com.example.gestordehorariosyclases

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class PerfilActivity : AppCompatActivity() {

    // -------------------------------------------------------
    // ------------------ VARIABLES GLOBALES ------------------
    // -------------------------------------------------------
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageButton

    // Componentes de la interfaz de perfil
    private lateinit var switchModoOscuro: SwitchMaterial
    private lateinit var btnExportar: Button
    private lateinit var btnImportar: Button
    private lateinit var btnGuardarPerfil: Button
    private lateinit var etNombrePerfil: EditText
    private lateinit var etCarreraPerfil: EditText
    private lateinit var imgPerfil: ImageView
    private lateinit var fabEditarPerfil: FloatingActionButton

    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences
    private var isEditing = false

    // -------------------------------------------------------
    // ----------- SELECTOR DE IMAGEN DE PERFIL ---------------
    // -------------------------------------------------------
    /**
     * Permite seleccionar una imagen de la galería para usarla como foto de perfil.
     * Guarda la URI de la imagen en SharedPreferences para uso futuro.
     */
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                try {
                    imgPerfil.setImageURI(uri)
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    prefs.edit().putString("user_photo_uri", uri.toString()).apply()
                    Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // -------------------------------------------------------
    // -------------------- MÉTODO ONCREATE -------------------
    // -------------------------------------------------------
    /**
     * Inicializa la pantalla de perfil:
     * - Carga los datos del usuario (nombre, carrera, foto).
     * - Configura el modo oscuro.
     * - Permite exportar/importar datos en formato JSON.
     * - Activa la navegación lateral.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Enlace de vistas
        switchModoOscuro = findViewById(R.id.switchModoOscuro)
        btnExportar = findViewById(R.id.btnExportar)
        btnImportar = findViewById(R.id.btnImportar)
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil)
        etNombrePerfil = findViewById(R.id.etNombrePerfil)
        etCarreraPerfil = findViewById(R.id.etCarreraPerfil)
        imgPerfil = findViewById(R.id.imgPerfil)
        fabEditarPerfil = findViewById(R.id.fabEditarPerfil)

        // Configurar perfil inicial
        cargarPerfil()
        btnGuardarPerfil.setOnClickListener { guardarPerfil() }

        // Seleccionar imagen de perfil solo si está en modo edición
        imgPerfil.setOnClickListener {
            if (isEditing) selectImageLauncher.launch("image/*")
        }

        fabEditarPerfil.setOnClickListener { toggleEdicionPerfil() }

        // Configurar modo oscuro
        val modoActual = AppCompatDelegate.getDefaultNightMode()
        switchModoOscuro.isChecked = (modoActual == AppCompatDelegate.MODE_NIGHT_YES)

        switchModoOscuro.setOnCheckedChangeListener { vista, isChecked ->
            if (vista.isPressed) {
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // Exportar e importar datos (backup JSON)
        btnExportar.setOnClickListener { exportarDatosJSON() }
        btnImportar.setOnClickListener { importarDatosJSON() }

        // Configurar menú lateral
        configurarMenu()
    }

    // -------------------------------------------------------
    // ------------------ PERFIL DE USUARIO -------------------
    // -------------------------------------------------------
    /**
     * Carga los datos del perfil almacenados en SharedPreferences
     * y muestra el nombre, carrera y foto de perfil.
     */
    private fun cargarPerfil() {
        val nombre = prefs.getString("user_name", "Estudiante")
        val carrera = prefs.getString("user_carrera", "Ingeniería en Sistemas")
        val photoUriString = prefs.getString("user_photo_uri", null)

        etNombrePerfil.setText(nombre)
        etCarreraPerfil.setText(carrera)

        if (photoUriString != null) {
            try {
                imgPerfil.setImageURI(Uri.parse(photoUriString))
            } catch (e: Exception) {
                imgPerfil.setImageResource(R.mipmap.ic_launcher_round)
            }
        }

        setCamposEditables(false)
    }

    /**
     * Guarda los datos modificados del perfil en SharedPreferences.
     */
    private fun guardarPerfil() {
        val nombre = etNombrePerfil.text.toString()
        val carrera = etCarreraPerfil.text.toString()

        prefs.edit().apply {
            putString("user_name", nombre)
            putString("user_carrera", carrera)
            apply()
        }

        Toast.makeText(this, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()
        toggleEdicionPerfil()
    }

    /**
     * Activa o desactiva el modo edición del perfil.
     * Permite cambiar el nombre, carrera y foto del usuario.
     */
    private fun toggleEdicionPerfil() {
        isEditing = !isEditing
        setCamposEditables(isEditing)

        if (isEditing) {
            btnGuardarPerfil.visibility = View.VISIBLE
            fabEditarPerfil.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            imgPerfil.isClickable = true
            Toast.makeText(this, "Toca la foto para cambiarla", Toast.LENGTH_SHORT).show()
        } else {
            btnGuardarPerfil.visibility = View.GONE
            fabEditarPerfil.setImageResource(R.drawable.ic_edit)
            imgPerfil.isClickable = false
            cargarPerfil()
            Toast.makeText(this, "Modo edición desactivado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Define si los campos del perfil son editables o de solo lectura.
     */
    private fun setCamposEditables(editable: Boolean) {
        etNombrePerfil.isFocusable = editable
        etNombrePerfil.isFocusableInTouchMode = editable
        etNombrePerfil.isCursorVisible = editable
        etCarreraPerfil.isFocusable = editable
        etCarreraPerfil.isFocusableInTouchMode = editable
        etCarreraPerfil.isCursorVisible = editable

        if (editable) etNombrePerfil.requestFocus()
        else {
            etNombrePerfil.clearFocus()
            etCarreraPerfil.clearFocus()
        }
    }

    // -------------------------------------------------------
    // ----------------- EXPORTAR E IMPORTAR -----------------
    // -------------------------------------------------------
    /**
     * Exporta las clases y tareas del usuario a un archivo JSON local.
     * Sirve como respaldo del sistema.
     */
    private fun exportarDatosJSON() {
        try {
            val clases = dbHelper.obtenerTodasLasClases()
            val tareas = dbHelper.obtenerTodasLasTareas()

            val jsonClases = JSONArray()
            for (c in clases) {
                val obj = JSONObject()
                obj.put("nombre", c.nombre)
                obj.put("profesor", c.profesor)
                obj.put("salon", c.salon)
                obj.put("dias", c.dias)
                obj.put("hora_inicio", c.horaInicio)
                obj.put("hora_fin", c.horaFin)
                obj.put("color", c.color)
                jsonClases.put(obj)
            }

            val jsonTareas = JSONArray()
            for (t in tareas) {
                val obj = JSONObject()
                obj.put("nombre", t.nombre)
                obj.put("fecha", t.fecha)
                obj.put("completada", t.completada)
                obj.put("tipo", t.tipo)
                obj.put("prioridad", t.prioridad)
                jsonTareas.put(obj)
            }

            val jsonFinal = JSONObject()
            jsonFinal.put("clases", jsonClases)
            jsonFinal.put("tareas", jsonTareas)

            val archivo = File(filesDir, "backup_horario.json")
            val writer = FileWriter(archivo)
            writer.write(jsonFinal.toString())
            writer.flush()
            writer.close()

            Toast.makeText(this, "Backup guardado exitosamente", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al exportar", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Importa los datos de un archivo JSON local
     * y los inserta nuevamente en la base de datos.
     */
    private fun importarDatosJSON() {
        val archivo = File(filesDir, "backup_horario.json")
        if (!archivo.exists()) {
            Toast.makeText(this, "No existe respaldo previo", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val reader = FileReader(archivo)
            val contenido = reader.readText()
            reader.close()

            val jsonFinal = JSONObject(contenido)
            val arrayClases = jsonFinal.getJSONArray("clases")

            for (i in 0 until arrayClases.length()) {
                val obj = arrayClases.getJSONObject(i)
                val clase = ClaseModelo(
                    nombre = obj.getString("nombre"),
                    profesor = obj.getString("profesor"),
                    salon = obj.getString("salon"),
                    dias = obj.getString("dias"),
                    horaInicio = obj.getString("hora_inicio"),
                    horaFin = obj.getString("hora_fin"),
                    color = if (obj.has("color")) obj.getString("color") else "#6200EE"
                )
                dbHelper.insertarClase(clase)
            }

            Toast.makeText(this, "Datos restaurados", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al importar", Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------------------------------
    // ------------------ CONFIGURAR MENÚ ---------------------
    // -------------------------------------------------------
    /**
     * Configura el menú lateral (Navigation Drawer)
     * y define las rutas de navegación entre las diferentes pantallas.
     */
    private fun configurarMenu() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)

        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeScreenActivity::class.java))
                R.id.nav_registro_clase -> startActivity(Intent(this, RegistroClasesActivity::class.java))
                R.id.nav_ver_horario -> startActivity(Intent(this, ListaClasesActivity::class.java))
                R.id.nav_nueva_tarea -> startActivity(Intent(this, AgregarTareaActivity::class.java))
                R.id.nav_ver_tareas -> startActivity(Intent(this, ListaTareasActivity::class.java))
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_perfil -> drawerLayout.closeDrawer(GravityCompat.START)
            }
            finish()
            true
        }
    }
}
