package com.example.gestordehorariosyclases

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para mostrar una lista de tareas en un RecyclerView.
 * Gestiona los eventos de eliminar, editar y marcar como completada.
 */
class TareaAdapter(
    private var listaTareas: ArrayList<TareaModelo>,
    private val onEliminarClick: (Int) -> Unit,             // Acción al eliminar una tarea
    private val onItemClick: (Int) -> Unit,                // Acción al hacer clic en una tarea
    private val onCheckClick: (Int, Boolean) -> Unit       // Acción al marcar/desmarcar completada
) : RecyclerView.Adapter<TareaAdapter.ViewHolder>() {

    /**
     * Clase interna que representa cada elemento (item) del RecyclerView.
     * Aquí se enlazan las vistas del layout item_tarea.xml.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreTarea)
        val tvMateria: TextView = view.findViewById(R.id.tvMateria)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val viewColor: View = view.findViewById(R.id.viewPrioridadColor)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarTarea)
        val cbCompletada: CheckBox = view.findViewById(R.id.checkBoxFinal)
    }


    //  CREACIÓN DE VISTAS

    /**
     * Infla el layout para cada elemento de la lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return ViewHolder(view)
    }

    //
    //  ASIGNACIÓN DE DATOS

    /**
     * Asigna los valores de cada tarea a su vista correspondiente.
     * También maneja la lógica de colores, tachado y acciones del usuario.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tarea = listaTareas[position]

        // Mostrar datos básicos
        holder.tvNombre.text = tarea.nombre
        holder.tvMateria.text = tarea.nombreClase
        holder.tvFecha.text = tarea.fecha
        holder.tvTipo.text = tarea.tipo.uppercase()

        // --- COLOR DE PRIORIDAD ---
        when (tarea.prioridad) {
            "Alta" -> holder.viewColor.setBackgroundColor(Color.parseColor("#F44336"))  // Rojo
            "Media" -> holder.viewColor.setBackgroundColor(Color.parseColor("#FFC107")) // Amarillo
            else -> holder.viewColor.setBackgroundColor(Color.parseColor("#4CAF50"))    // Verde
        }

        // --- ESTADO DE COMPLETADO ---
        // Evita que se dispare el listener al actualizar visualmente
        holder.cbCompletada.setOnCheckedChangeListener(null)
        holder.cbCompletada.isChecked = tarea.completada

        // Efecto visual si está completada
        if (tarea.completada) {
            holder.tvNombre.paintFlags = holder.tvNombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvNombre.alpha = 0.5f
        } else {
            holder.tvNombre.paintFlags = holder.tvNombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvNombre.alpha = 1.0f
        }

        // Listener del checkbox marcar completada
        holder.cbCompletada.setOnCheckedChangeListener { _, isChecked ->
            onCheckClick(tarea.id, isChecked)
            // Actualiza la apariencia inmediatamente
            if (isChecked) {
                holder.tvNombre.paintFlags = holder.tvNombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvNombre.alpha = 0.5f
            } else {
                holder.tvNombre.paintFlags = holder.tvNombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.tvNombre.alpha = 1.0f
            }
        }

        // Botón eliminar
        holder.btnEliminar.setOnClickListener { onEliminarClick(tarea.id) }

        // Clic en el item completo para editar o ver detalles
        holder.itemView.setOnClickListener { onItemClick(tarea.id) }
    }


    //  TAMAÑO DE LA LISTA

    /**
     * Retorna el número total de tareas en la lista.
     */
    override fun getItemCount() = listaTareas.size


    //  ACTUALIZAR LA LISTA

    /**
     * Actualiza la lista de tareas mostrada en pantalla.
     * Se usa al agregar, eliminar o modificar tareas.
     */
    fun actualizarLista(nuevaLista: ArrayList<TareaModelo>) {
        listaTareas = nuevaLista
        notifyDataSetChanged()
    }
}
