package com.example.gestordehorariosyclases

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.util.Calendar

/**
 * Clase que administra la base de datos local del gestor de horarios y tareas.
 * Contiene la definición de las tablas, operaciones CRUD y reportes estadísticos.
 *
 * @param context Contexto de la aplicación (requerido por SQLiteOpenHelper)
 */
class BaseDeDatosHelper(context: Context) : SQLiteOpenHelper(context, "Horario.db", null, 1) {

    // -------------------------------------------------------
    // ------------------ CONFIGURACIÓN INICIAL ----------------
    // -------------------------------------------------------

    /**
     * Habilita el soporte de claves foráneas para mantener integridad referencial.
     */
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Se ejecuta al crear por primera vez la base de datos.
     * Define las tablas: `clases` y `tareas`.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        val crearTablaClases = """
            CREATE TABLE clases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                profesor TEXT,
                salon TEXT,
                dias TEXT,
                hora_inicio TEXT,
                hora_fin TEXT
            )
        """
        db?.execSQL(crearTablaClases)

        val crearTablaTareas = """
            CREATE TABLE tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_tarea TEXT,
                fecha_limite TEXT,
                id_clase INTEGER,
                completada INTEGER DEFAULT 0,
                tipo TEXT,
                prioridad TEXT,
                FOREIGN KEY(id_clase) REFERENCES clases(id) ON DELETE CASCADE
            )
        """
        db?.execSQL(crearTablaTareas)
    }

    /**
     * Se ejecuta cuando se actualiza la versión de la base de datos.
     * Borra las tablas existentes y las vuelve a crear.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS clases")
        db?.execSQL("DROP TABLE IF EXISTS tareas")
        onCreate(db)
    }

    // -------------------------------------------------------
    // ------------------ CRUD DE CLASES ----------------------
    // -------------------------------------------------------

    /**
     * Inserta una nueva clase en la tabla `clases`.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    fun insertarClase(clase: ClaseModelo): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("nombre", clase.nombre)
            put("profesor", clase.profesor)
            put("salon", clase.salon)
            put("dias", clase.dias)
            put("hora_inicio", clase.horaInicio)
            put("hora_fin", clase.horaFin)
        }
        val resultado = db.insert("clases", null, valores)
        db.close()
        return resultado != -1L
    }

    /**
     * Obtiene todas las clases registradas en la base de datos.
     * @return Lista de objetos ClaseModelo.
     */
    fun obtenerTodasLasClases(): ArrayList<ClaseModelo> {
        val lista = ArrayList<ClaseModelo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM clases", null)

        if (cursor.moveToFirst()) {
            do {
                val clase = ClaseModelo(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    profesor = cursor.getString(2),
                    salon = cursor.getString(3),
                    dias = cursor.getString(4),
                    horaInicio = cursor.getString(5),
                    horaFin = cursor.getString(6)
                )
                lista.add(clase)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    /**
     * Obtiene una clase específica por su ID.
     */
    fun obtenerClase(id: Int): ClaseModelo? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM clases WHERE id = ?", arrayOf(id.toString()))
        var clase: ClaseModelo? = null

        if (cursor.moveToFirst()) {
            clase = ClaseModelo(
                id = cursor.getInt(0),
                nombre = cursor.getString(1),
                profesor = cursor.getString(2),
                salon = cursor.getString(3),
                dias = cursor.getString(4),
                horaInicio = cursor.getString(5),
                horaFin = cursor.getString(6)
            )
        }

        cursor.close()
        db.close()
        return clase
    }

    /**
     * Actualiza los datos de una clase existente.
     */
    fun actualizarClase(clase: ClaseModelo): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("nombre", clase.nombre)
            put("profesor", clase.profesor)
            put("salon", clase.salon)
            put("dias", clase.dias)
            put("hora_inicio", clase.horaInicio)
            put("hora_fin", clase.horaFin)
        }
        val filas = db.update("clases", valores, "id=?", arrayOf(clase.id.toString()))
        db.close()
        return filas > 0
    }

    /**
     * Elimina una clase y sus tareas asociadas.
     */
    fun eliminarClase(id: Int): Boolean {
        val db = writableDatabase
        val filas = db.delete("clases", "id=?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }

    // -------------------------------------------------------
    // ------------------ CRUD DE TAREAS ----------------------
    // -------------------------------------------------------

    /**
     * Inserta una nueva tarea en la tabla `tareas`.
     */
    fun insertarTarea(tarea: TareaModelo): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("nombre_tarea", tarea.nombre)
            put("fecha_limite", tarea.fecha)
            put("id_clase", tarea.idClase)
            put("completada", if (tarea.completada) 1 else 0)
            put("tipo", tarea.tipo)
            put("prioridad", tarea.prioridad)
        }
        val resultado = db.insert("tareas", null, valores)
        db.close()
        return resultado != -1L
    }

    /**
     * Devuelve una lista con todas las tareas y su clase correspondiente.
     */
    fun obtenerTodasLasTareas(): ArrayList<TareaModelo> {
        val lista = ArrayList<TareaModelo>()
        val db = readableDatabase
        val query = """
            SELECT t.id, t.nombre_tarea, t.fecha_limite, t.id_clase, t.completada,
                   c.nombre, t.tipo, t.prioridad
            FROM tareas t
            INNER JOIN clases c ON t.id_clase = c.id
        """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val tarea = TareaModelo(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    fecha = cursor.getString(2),
                    idClase = cursor.getInt(3),
                    completada = cursor.getInt(4) == 1,
                    nombreClase = cursor.getString(5),
                    tipo = cursor.getString(6),
                    prioridad = cursor.getString(7)
                )
                lista.add(tarea)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    /**
     * Obtiene una tarea específica según su ID.
     */
    fun obtenerTarea(id: Int): TareaModelo? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tareas WHERE id = ?", arrayOf(id.toString()))
        var tarea: TareaModelo? = null

        if (cursor.moveToFirst()) {
            tarea = TareaModelo(
                id = cursor.getInt(0),
                nombre = cursor.getString(1),
                fecha = cursor.getString(2),
                idClase = cursor.getInt(3),
                completada = cursor.getInt(4) == 1,
                tipo = cursor.getString(5),
                prioridad = cursor.getString(6)
            )
        }

        cursor.close()
        db.close()
        return tarea
    }

    /**
     * Actualiza los datos principales de una tarea (nombre, fecha, tipo, prioridad).
     */
    fun actualizarTarea(tarea: TareaModelo): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("nombre_tarea", tarea.nombre)
            put("fecha_limite", tarea.fecha)
            put("id_clase", tarea.idClase)
            put("tipo", tarea.tipo)
            put("prioridad", tarea.prioridad)
        }
        val filas = db.update("tareas", valores, "id=?", arrayOf(tarea.id.toString()))
        db.close()
        return filas > 0
    }

    /**
     * Elimina una tarea por su ID.
     */
    fun eliminarTarea(id: Int): Boolean {
        val db = writableDatabase
        val filas = db.delete("tareas", "id=?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }

    /**
     * Cambia el estado de completada/pending de una tarea.
     */
    fun actualizarEstadoTarea(id: Int, completada: Boolean): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("completada", if (completada) 1 else 0)
        }
        val filas = db.update("tareas", valores, "id=?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }

    // -------------------------------------------------------
    // ------------------ REPORTES Y DASHBOARD ----------------
    // -------------------------------------------------------

    /** Devuelve el número total de clases registradas. */
    fun contarClases(): Int = ejecutarConteo("SELECT COUNT(*) FROM clases")

    /** Devuelve el número de tareas pendientes. */
    fun contarTareasPendientes(): Int = ejecutarConteo("SELECT COUNT(*) FROM tareas WHERE completada = 0")

    /** Devuelve el número de tareas completadas. */
    fun contarTareasCompletadas(): Int = ejecutarConteo("SELECT COUNT(*) FROM tareas WHERE completada = 1")

    /**
     * Devuelve la próxima tarea pendiente ordenada por fecha.
     */
    fun obtenerProximaTarea(): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT nombre_tarea, fecha_limite FROM tareas WHERE completada = 0 ORDER BY fecha_limite ASC LIMIT 1",
            null
        )
        var nombreTarea = "Sin tareas pendientes"
        if (cursor.moveToFirst()) {
            nombreTarea = "${cursor.getString(0)} (${cursor.getString(1)})"
        }
        cursor.close()
        db.close()
        return nombreTarea
    }

    /**
     * Calcula el total de horas semanales con base en las clases registradas.
     * Retorna un formato legible (por ejemplo: "8h 30m").
     */
    fun calcularHorasSemanales(): String {
        var totalMinutos = 0
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT dias, hora_inicio, hora_fin FROM clases", null)

        if (cursor.moveToFirst()) {
            do {
                val dias = cursor.getString(0)
                val inicio = cursor.getString(1)
                val fin = cursor.getString(2)
                val numeroDias = if (dias.isBlank()) 0 else dias.split(",").size

                try {
                    val (hInicio, mInicio) = inicio.split(":").map { it.toInt() }
                    val (hFin, mFin) = fin.split(":").map { it.toInt() }
                    var duracion = (hFin * 60 + mFin) - (hInicio * 60 + mInicio)
                    if (duracion < 0) duracion += 1440
                    totalMinutos += duracion * numeroDias
                } catch (_: Exception) {}
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        val horas = totalMinutos / 60
        val minutos = totalMinutos % 60
        return when {
            horas > 0 && minutos > 0 -> "${horas}h ${minutos}m"
            horas > 0 -> "${horas}h"
            minutos > 0 -> "${minutos}m"
            else -> "0"
        }
    }

    /**
     * Devuelve una lista con el número de tareas pendientes por clase.
     */
    fun obtenerDesgloseTareas(): ArrayList<String> {
        val lista = ArrayList<String>()
        val db = readableDatabase
        val query = """
            SELECT c.nombre, COUNT(t.id)
            FROM clases c
            LEFT JOIN tareas t ON c.id = t.id_clase AND t.completada = 0
            GROUP BY c.id
        """
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(0)
                val cantidad = cursor.getInt(1)
                if (cantidad > 0) lista.add("$nombre: $cantidad")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    /**
     * Devuelve cuántas clases hay el día actual.
     */
    fun contarClasesHoy(): Int {
        val calendario = Calendar.getInstance()
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
        val hoy = when (diaSemana) {
            Calendar.MONDAY -> "Lun"
            Calendar.TUESDAY -> "Mar"
            Calendar.WEDNESDAY -> "Mié"
            Calendar.THURSDAY -> "Jue"
            Calendar.FRIDAY -> "Vie"
            Calendar.SATURDAY -> "Sáb"
            else -> "Dom"
        }
        return ejecutarConteo("SELECT COUNT(*) FROM clases WHERE dias LIKE '%$hoy%'")
    }

    /**
     * Obtiene las tareas pendientes ordenadas por fecha, con nombre de clase incluido.
     */
    fun obtenerTareasPendientesDetalladas(): ArrayList<TareaModelo> {
        val lista = ArrayList<TareaModelo>()
        val db = readableDatabase
        val query = """
            SELECT t.id, t.nombre_tarea, t.fecha_limite, t.id_clase, t.completada,
                   c.nombre, t.tipo, t.prioridad
            FROM tareas t
            INNER JOIN clases c ON t.id_clase = c.id
            WHERE t.completada = 0
            ORDER BY t.fecha_limite ASC
        """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val tarea = TareaModelo(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    fecha = cursor.getString(2),
                    idClase = cursor.getInt(3),
                    completada = cursor.getInt(4) == 1,
                    nombreClase = cursor.getString(5),
                    tipo = cursor.getString(6),
                    prioridad = cursor.getString(7)
                )
                lista.add(tarea)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    // -------------------------------------------------------
    // ------------------ FUNCIONES AUXILIARES ----------------
    // -------------------------------------------------------

    /**
     * Ejecuta una consulta COUNT(*) y devuelve el resultado.
     */
    private fun ejecutarConteo(query: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }
}
