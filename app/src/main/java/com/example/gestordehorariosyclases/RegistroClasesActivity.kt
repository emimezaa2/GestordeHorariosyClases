package com.example.gestordehorariosyclases

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.Calendar

class RegistroClasesActivity : AppCompatActivity() {


    // VARIABLES

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageButton

    // Campos de texto para los datos de la clase
    private lateinit var etNombre: EditText
    private lateinit var etProfesor: EditText
    private lateinit var etSalon: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var btnGuardar: Button

    // Días de la semana
    private lateinit var cbLun: CheckBox
    private lateinit var cbMar: CheckBox
    private lateinit var cbMie: CheckBox
    private lateinit var cbJue: CheckBox
    private lateinit var cbVie: CheckBox
    private lateinit var cbSab: CheckBox

    // Grupo de colores
    private lateinit var rgColorClase: RadioGroup

    // Base de datos y preferencias
    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences

    private var idClaseEditar: Int? = null // Si existe, estamos editando una clase existente


    //  MÉTODO ONCREATE

    /**
     * Inicializa la vista para registrar o editar una clase:
     * - Configura los elementos visuales.
     * - Activa los selectores de hora.
     * - Carga los datos si se va a editar una clase.
     * - Configura el menú lateral.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_clases)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Inicialización de vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)

        etNombre = findViewById(R.id.etNombreClase)
        etProfesor = findViewById(R.id.etProfesor)
        etSalon = findViewById(R.id.etSalon)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFin)
        btnGuardar = findViewById(R.id.btnGuardar)

        cbLun = findViewById(R.id.cbLun)
        cbMar = findViewById(R.id.cbMar)
        cbMie = findViewById(R.id.cbMie)
        cbJue = findViewById(R.id.cbJue)
        cbVie = findViewById(R.id.cbVie)
        cbSab = findViewById(R.id.cbSab)

        rgColorClase = findViewById(R.id.rgColorClase)

        // Configuración del menú lateral
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        configurarMenu()

        // Selectores de hora
        etHoraInicio.setOnClickListener { mostrarReloj(etHoraInicio) }
        etHoraFin.setOnClickListener { mostrarReloj(etHoraFin) }

        // Si venimos desde otra pantalla con ID_CLASE, se cargan los datos para editar
        if (intent.hasExtra("ID_CLASE")) {
            idClaseEditar = intent.getIntExtra("ID_CLASE", 0)
            cargarDatosParaEditar(idClaseEditar!!)
        }

        btnGuardar.setOnClickListener { guardarClase() }
    }

    //  MÉTODO ONRESUME

    /**
     * Se ejecuta cada vez que regresamos a esta pantalla.
     * Actualiza el encabezado del menú lateral con el nombre y foto del usuario.
     */
    override fun onResume() {
        super.onResume()
        actualizarNavHeader()
    }


    //  ACTUALIZAR SIDEBAR

    /**
     * Carga los datos del usuario en el menú lateral (nombre, carrera y foto).
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


    //  MOSTRAR RELOJ

    /**
     * Abre un diálogo para seleccionar una hora y la asigna al EditText indicado.
     */
    private fun mostrarReloj(editText: EditText) {
        val calendario = Calendar.getInstance()
        val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendario.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, hora, minuto ->
            editText.setText(String.format("%02d:%02d", hora, minuto))
        }, horaActual, minutoActual, true)

        timePicker.show()
    }


    //  OBTENER DÍAS SELECCIONADOS

    /**
     * Devuelve una cadena con los días seleccionados en formato "Lun, Mar, Mié..."
     */
    private fun obtenerDiasSeleccionados(): String {
        val dias = ArrayList<String>()
        if (cbLun.isChecked) dias.add("Lun")
        if (cbMar.isChecked) dias.add("Mar")
        if (cbMie.isChecked) dias.add("Mié")
        if (cbJue.isChecked) dias.add("Jue")
        if (cbVie.isChecked) dias.add("Vie")
        if (cbSab.isChecked) dias.add("Sáb")
        return dias.joinToString(", ")
    }

    /**
     * Marca los CheckBox según los días guardados en el registro.
     */
    private fun marcarDiasEnCheckbox(diasString: String) {
        cbLun.isChecked = diasString.contains("Lun")
        cbMar.isChecked = diasString.contains("Mar")
        cbMie.isChecked = diasString.contains("Mié")
        cbJue.isChecked = diasString.contains("Jue")
        cbVie.isChecked = diasString.contains("Vie")
        cbSab.isChecked = diasString.contains("Sáb")
    }


    //  CARGAR DATOS PARA EDITAR

    /**
     * Si se edita una clase existente, carga sus datos en los campos del formulario
     */
    private fun cargarDatosParaEditar(id: Int) {
        val clase = dbHelper.obtenerClase(id)
        if (clase != null) {
            etNombre.setText(clase.nombre)
            etProfesor.setText(clase.profesor)
            etSalon.setText(clase.salon)
            etHoraInicio.setText(clase.horaInicio)
            etHoraFin.setText(clase.horaFin)
            marcarDiasEnCheckbox(clase.dias)

            // Seleccionar el color correspondiente
            when (clase.color) {
                "#2196F3" -> findViewById<RadioButton>(R.id.rbAzul).isChecked = true
                "#E91E63" -> findViewById<RadioButton>(R.id.rbRosa).isChecked = true
                "#FF9800" -> findViewById<RadioButton>(R.id.rbNaranja).isChecked = true
                "#4CAF50" -> findViewById<RadioButton>(R.id.rbVerde).isChecked = true
            }

            btnGuardar.text = "Actualizar Clase"
        }
    }

    //
    //  GUARDAR CLASE

    /**
     * Valida los datos ingresados y guarda la clase en la base de datos.
     * Si ya existe, actualiza los datos.
     */
    private fun guardarClase() {
        val nombre = etNombre.text.toString()
        val profesor = etProfesor.text.toString()
        val salon = etSalon.text.toString()
        val dias = obtenerDiasSeleccionados()
        val horaInicio = etHoraInicio.text.toString()
        val horaFin = etHoraFin.text.toString()

        // Determinar color
        val idColor = rgColorClase.checkedRadioButtonId
        val colorHex = when (idColor) {
            R.id.rbAzul -> "#2196F3"
            R.id.rbRosa -> "#E91E63"
            R.id.rbNaranja -> "#FF9800"
            R.id.rbVerde -> "#4CAF50"
            else -> "#6200EE"
        }

        if (nombre.isEmpty() || dias.isEmpty() || horaInicio.isEmpty()) {
            Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            return
        }

        val clase = ClaseModelo(
            id = idClaseEditar ?: 0,
            nombre = nombre,
            profesor = profesor,
            salon = salon,
            dias = dias,
            horaInicio = horaInicio,
            horaFin = horaFin,
            color = colorHex
        )

        val exito = if (idClaseEditar == null) {
            dbHelper.insertarClase(clase).also {
                if (it) {
                    Toast.makeText(this, "Clase guardada", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                }
            }
        } else {
            dbHelper.actualizarClase(clase).also {
                if (it) Toast.makeText(this, "Clase actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        if (!exito) Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
    }


    /**
     * Limpia los campos del formulario después de guardar una clase nueva.
     */
    private fun limpiarCampos() {
        etNombre.text.clear()
        etProfesor.text.clear()
        etSalon.text.clear()
        etHoraInicio.text.clear()
        etHoraFin.text.clear()
        cbLun.isChecked = false
        cbMar.isChecked = false
        cbMie.isChecked = false
        cbJue.isChecked = false
        cbVie.isChecked = false
        cbSab.isChecked = false
        etNombre.requestFocus()
    }


    /**
     * Configura el menú lateral (Navigation Drawer)
     * y permite navegar entre las diferentes pantallas del sistema.
     */
    private fun configurarMenu() {
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeScreenActivity::class.java))
                R.id.nav_registro_clase -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_ver_horario -> startActivity(Intent(this, ListaClasesActivity::class.java))
                R.id.nav_nueva_tarea -> startActivity(Intent(this, AgregarTareaActivity::class.java))
                R.id.nav_ver_tareas -> startActivity(Intent(this, ListaTareasActivity::class.java))
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
            }
            finish()
            true
        }
    }
}
