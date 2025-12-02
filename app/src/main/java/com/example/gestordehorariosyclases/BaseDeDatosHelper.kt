package com.example.gestordehorariosyclases

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.util.Calendar

/**
 * Clase encargada de manejar la base de datos del gestor de horarios y tareas.
 * Aquí se crean las tablas, se guardan los datos y se hacen las consultas.
 * También incluye funciones para obtener estadísticas simples.
 */
class BaseDeDatosHelper(context: Context) : SQLiteOpenHelper(context, "Horario.db", null, 1) {



    // Se crean las tablas de clases y tareas cuando se instala la app por primera vez
    override fun onCreate(db: SQLiteDatabase?) {
        val crearTablaClases = """
            CREATE TABLE clases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                profesor TEXT,
                salon TEXT,
                dias TEXT,
                hora_inicio TEXT,
                hora_fin TEXT,
                color TEXT
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

    // Si se actualiza la versión de la base de datos, se eliminan las tablas viejas y se crean nuevas
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS clases")
        db?.execSQL("DROP TABLE IF EXISTS tareas")
        onCreate(db)
    }

    //
    //  CRUD DE CLASES
    //

    /**
     * Inserta una nueva clase en la base de datos.
     * Devuelve true si se guardó correctamente.
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
            put("color", clase.color)
        }
        val resultado = db.insert("clases", null, valores)
        db.close()
        return resultado != -1L
    }

    /**
     * Devuelve todas las clases guardadas.
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
                    horaFin = cursor.getString(6),
                    color = cursor.getString(7)
                )
                lista.add(clase)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    /**
     * Devuelve una clase específica según su id.
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
                horaFin = cursor.getString(6),
                color = cursor.getString(7)
            )
        }

        cursor.close()
        db.close()
        return clase
    }

    /**
     * Actualiza los datos de una clase ya guardada.
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
            put("color", clase.color)
        }
        val filas = db.update("clases", valores, "id=?", arrayOf(clase.id.toString()))
        db.close()
        return filas > 0
    }

    /**
     * Elimina una clase de la base de datos.
     * Si la clase tiene tareas, también se eliminan por la relación foránea.
     */
    fun eliminarClase(id: Int): Boolean {
        val db = writableDatabase
        val filas = db.delete("clases", "id=?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }


    // Se activa el soporte de llaves foráneas para que al eliminar una clase también se borren sus tareas
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    //  CRUD DE TAREAS
    //

    /**
     * Inserta una nueva tarea.
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
     * Devuelve todas las tareas junto con el nombre de la clase a la que pertenecen.
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
     * Obtiene una tarea especifica por su id.
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
     * Actualiza la informacion de una tarea.
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
     * Elimina una tarea de la base de datos.
     */
    fun eliminarTarea(id: Int): Boolean {
        val db = writableDatabase
        val filas = db.delete("tareas", "id=?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }

    /**
     * Cambia el estado de una tarea (completada o no completada).
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

    /** Cuenta cuantas clases hay registradas. */
    fun contarClases(): Int = ejecutarConteo("SELECT COUNT(*) FROM clases")

    /** Cuenta cuantas tareas estan pendientes. */
    fun contarTareasPendientes(): Int = ejecutarConteo("SELECT COUNT(*) FROM tareas WHERE completada = 0")

    /** Cuenta cuantas tareas ya estan marcadas como completadas. */
    fun contarTareasCompletadas(): Int = ejecutarConteo("SELECT COUNT(*) FROM tareas WHERE completada = 1")

    /**
     * Devuelve el nombre y fecha de la tarea mas cercana a vencer.
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
     * Calcula las horas totales de clase a la semana segun los horarios registrados.
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
     * Devuelve una lista con el nombre de cada clase y cuantas tareas pendientes tiene.
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
     * Cuenta cuantas clases hay hoy segun el dia actual del calendario.
     */
    fun contarClasesHoy(): Int {
        val calendario = Calendar.getInstance()
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
        val hoy = when (diaSemana) {
            Calendar.MONDAY -> "Lun"
            Calendar.TUESDAY -> "Mar"
            Calendar.WEDNESDAY -> "Mie"
            Calendar.THURSDAY -> "Jue"
            Calendar.FRIDAY -> "Vie"
            Calendar.SATURDAY -> "Sab"
            else -> "Dom"
        }
        return ejecutarConteo("SELECT COUNT(*) FROM clases WHERE dias LIKE '%$hoy%'")
    }

    /**
     * Devuelve una lista con todas las tareas pendientes ordenadas por fecha.
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
     * Ejecuta una consulta de conteo y devuelve el resultado.
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
