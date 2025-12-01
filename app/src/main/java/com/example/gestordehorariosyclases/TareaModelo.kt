package com.example.gestordehorariosyclases

/**
 * Representa una tarea o actividad registrada por el usuario.
 * Esta clase se utiliza como modelo de datos dentro del sistema.
 *
 * @property id Identificador único de la tarea autoincremental en la base de datos
 * @property nombre Nombre o título de la tarea.
 * @property fecha Fecha límite o de entrega de la tarea formato
 * @property idClase Identificador de la clase a la que pertenece la tarea.
 * @property nombreClase Nombre de la clase asociada (para mostrar en la interfaz).
 * @property completada Indica si la tarea ya fue completada (true) o no (false).
 * @property tipo Tipo de tarea, puede ser "Tarea" o "Examen".
 * @property prioridad Nivel de prioridad: "Alta", "Media" o "Baja".
 */
data class TareaModelo(
    var id: Int = 0,
    var nombre: String = "",
    var fecha: String = "",
    var idClase: Int = 0,
    var nombreClase: String = "",
    var completada: Boolean = false,
    var tipo: String = "Tarea",     //
    var prioridad: String = "Baja"  //
)
