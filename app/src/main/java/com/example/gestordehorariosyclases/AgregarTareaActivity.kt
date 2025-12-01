package com.example.gestordehorariosyclases

import android.app.DatePickerDialog
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

/**
 * Pantalla para agregar o editar tareas en el sistema.
 * Permite registrar nombre, fecha, tipo, prioridad y clase asociada.
 */
class AgregarTareaActivity : AppCompatActivity() {


    //  VARIABLES

    private lateinit var etNombre: EditText
    private lateinit var etFecha: EditText
    private lateinit var spinnerClases: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var rgTipo: RadioGroup
    private lateinit var rgPrioridad: RadioGroup

    // Menú lateral
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageButton

    private lateinit var dbHelper: BaseDeDatosHelper
    private lateinit var prefs: SharedPreferences

    private var listaClases = ArrayList<ClaseModelo>()
    private var idTareaEditar: Int? = null




    /**
     * Inicializa la actividad y prepara los componentes:
     * - Carga vistas.
     * - Configura el menú lateral.
     * - Llena el spinner con clases registradas.
     * - Si se recibe un ID, carga la tarea para edición.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_tarea)

        dbHelper = BaseDeDatosHelper(this)
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        inicializarVistas()
        configurarMenu()
        cargarClasesEnSpinner()

        // Si se viene desde otra pantalla con una tarea existente
        if (intent.hasExtra("ID_TAREA")) {
            idTareaEditar = intent.getIntExtra("ID_TAREA", 0)
            cargarDatosTarea(idTareaEditar!!)
        }

        // Configura eventos
        etFecha.setOnClickListener { mostrarCalendario() }
        btnGuardar.setOnClickListener { guardarTarea() }
    }


    /**
     * Se ejecuta al volver a esta pantalla.
     * Refresca los datos del menú lateral (nombre, carrera, foto).
     */
    override fun onResume() {
        super.onResume()
        actualizarNavHeader()
    }


    //INICIALIZAR VISTAS

    /**
     * Asocia las variables con sus elementos del XML.
     */
    private fun inicializarVistas() {
        etNombre = findViewById(R.id.etNombreTarea)
        etFecha = findViewById(R.id.etFechaTarea)
        spinnerClases = findViewById(R.id.spinnerClases)
        btnGuardar = findViewById(R.id.btnGuardarTarea)
        rgTipo = findViewById(R.id.rgTipo)
        rgPrioridad = findViewById(R.id.rgPrioridad)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
    }


    //  ACTUALIZAR MENÚ LATERAL

    /**
     * Actualiza la foto, nombre y carrera mostrados en el encabezado del menú lateral.
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


    //  CARGAR DATOS DE TAREA
    /**
     * Carga los datos de una tarea existente en el formulario
     * cuando el usuario la edita.
     */
    private fun cargarDatosTarea(id: Int) {
        val tarea = dbHelper.obtenerTarea(id) ?: return

        etNombre.setText(tarea.nombre)
        etFecha.setText(tarea.fecha)
        btnGuardar.text = "Actualizar Actividad"

        // Tipo de tarea
        if (tarea.tipo == "Examen") {
            findViewById<RadioButton>(R.id.rbExamen).isChecked = true
        } else {
            findViewById<RadioButton>(R.id.rbTarea).isChecked = true
        }

        // Prioridad
        when (tarea.prioridad) {
            "Alta" -> findViewById<RadioButton>(R.id.rbAlta).isChecked = true
            "Media" -> findViewById<RadioButton>(R.id.rbMedia).isChecked = true
            else -> findViewById<RadioButton>(R.id.rbBaja).isChecked = true
        }
    }


    //  MOSTRAR CALENDARIO

    /**
     * Abre un selector de fecha (DatePickerDialog)
     * y coloca la fecha seleccionada en el campo correspondiente.
     */
    private fun mostrarCalendario() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val fechaFormateada = "$day/${month + 1}/$year"
            etFecha.setText(fechaFormateada)
        }, anio, mes, dia)

        datePicker.show()
    }

    //
    //  GUARDAR TAREA

    /**
     * Valida los datos del formulario y guarda una nueva tarea o actualiza una existente.
     * - Verifica que haya nombre, fecha y clases disponibles.
     * - Crea un objeto TareaModelo y lo guarda en la base de datos.
     */
    private fun guardarTarea() {
        val nombre = etNombre.text.toString()
        val fecha = etFecha.text.toString()

        // Validación de campos obligatorios
        if (nombre.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(this, "Por favor completa los datos", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaClases.isEmpty()) {
            Toast.makeText(this, "Registra primero una clase", Toast.LENGTH_SHORT).show()
            return
        }

        // Datos seleccionados por el usuario
        val claseSeleccionada = listaClases[spinnerClases.selectedItemPosition]
        val tipoTexto = findViewById<RadioButton>(rgTipo.checkedRadioButtonId).text.toString()
        val prioridadTexto = findViewById<RadioButton>(rgPrioridad.checkedRadioButtonId).text.toString()

        val nuevaTarea = TareaModelo(
            id = idTareaEditar ?: 0,
            nombre = nombre,
            fecha = fecha,
            idClase = claseSeleccionada.id,
            tipo = tipoTexto,
            prioridad = prioridadTexto
        )

        // Inserta o actualiza la tarea según el caso
        val exito = if (idTareaEditar == null) {
            dbHelper.insertarTarea(nuevaTarea)
        } else {
            dbHelper.actualizarTarea(nuevaTarea)
        }

        // Resultado
        if (exito) {
            Toast.makeText(
                this,
                if (idTareaEditar == null) "Agendada" else "Actualizada",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this, ListaTareasActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }


    //  CARGAR CLASES EN SPINNER

    /**
     * Llena el spinner con los nombres de las clases registradas
     * para que el usuario asocie una tarea a una clase.
     */
    private fun cargarClasesEnSpinner() {
        listaClases = dbHelper.obtenerTodasLasClases()
        val nombres = listaClases.map { it.nombre }

        if (nombres.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerClases.adapter = adapter
        }
    }


    /**
     * Configura la navegación del menú latera
     * Permite moverse entre las distintas pantallas del sistema.
     */
    private fun configurarMenu() {
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeScreenActivity::class.java))
                R.id.nav_registro_clase -> startActivity(Intent(this, RegistroClasesActivity::class.java))
                R.id.nav_ver_horario -> startActivity(Intent(this, ListaClasesActivity::class.java))
                R.id.nav_nueva_tarea -> drawerLayout.closeDrawer(GravityCompat.START) // Ya estamos aquí
                R.id.nav_ver_tareas -> startActivity(Intent(this, ListaTareasActivity::class.java))
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
            }
            finish()
            true
        }
    }
}
