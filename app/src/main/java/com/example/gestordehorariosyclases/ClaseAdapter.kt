package com.example.gestordehorariosyclases

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para mostrar la lista de clases registradas en un RecyclerView.
 * Administra los eventos de clic, eliminación y la actualización de datos.
 *
 * @param listaClases Lista de objetos ClaseModelo a mostrar.
 * @param onEliminarClick Acción que se ejecuta al presionar el botón de eliminar.
 * @param onItemClick Acción que se ejecuta al hacer clic en una clase (para editarla).
 */
class ClaseAdapter(
    private var listaClases: ArrayList<ClaseModelo>,
    private val onEliminarClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ClaseAdapter.ViewHolder>() {

    // -------------------------------------------------------
    // -------------------- VIEW HOLDER -----------------------
    // -------------------------------------------------------

    /**
     * Contiene las referencias a los elementos del diseño (item_clase.xml)
     * que se usarán para mostrar los datos de cada clase.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreClase)
        val tvProfesor: TextView = view.findViewById(R.id.tvProfesor)
        val tvSalon: TextView = view.findViewById(R.id.tvSalon)
        val tvHorario: TextView = view.findViewById(R.id.tvHorario)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
        val viewColor: View = view.findViewById(R.id.viewColorClase)
    }

    // -------------------------------------------------------
    // -------------------- ADAPTER CORE ----------------------
    // -------------------------------------------------------

    /**
     * Infla el diseño XML correspondiente a cada elemento de la lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clase, parent, false)
        return ViewHolder(view)
    }

    /**
     * Asocia los datos de cada objeto ClaseModelo con los elementos de la interfaz.
     * También configura los eventos de clic y eliminación.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clase = listaClases[position]

        // Asignación de datos a la vista
        holder.tvNombre.text = clase.nombre
        holder.tvProfesor.text = "Prof: ${clase.profesor}"
        holder.tvSalon.text = "Salón: ${clase.salon}"
        holder.tvHorario.text = "${clase.dias} ${clase.horaInicio}-${clase.horaFin}"

        // Aplica el color de la clase (barra lateral)
        val color = try {
            Color.parseColor(clase.color)
        } catch (_: Exception) {
            Color.parseColor("#6200EE") // Color por defecto si hay error
        }
        holder.viewColor.setBackgroundColor(color)

        // Configura los eventos
        holder.btnEliminar.setOnClickListener { onEliminarClick(clase.id) }
        holder.itemView.setOnClickListener { onItemClick(clase.id) }
    }

    /**
     * Devuelve el número total de elementos en la lista.
     */
    override fun getItemCount(): Int = listaClases.size

    // -------------------------------------------------------
    // -------------------- MÉTODO EXTRA ----------------------
    // -------------------------------------------------------

    /**
     * Reemplaza la lista actual de clases por una nueva y notifica al RecyclerView
     * para que refresque los datos visualmente.
     *
     * @param nuevaLista Nueva lista de clases obtenida desde la base de datos.
     */
    fun actualizarLista(nuevaLista: ArrayList<ClaseModelo>) {
        listaClases = nuevaLista
        notifyDataSetChanged()
    }
}
