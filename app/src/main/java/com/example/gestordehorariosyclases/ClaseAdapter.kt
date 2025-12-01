package com.example.gestordehorariosyclases

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador que muestra la lista de clases dentro del RecyclerView.
 * Se encarga de enlazar los datos de cada clase con su tarjeta visual.
 * También maneja los clics para editar o eliminar una clase.
 *
 * @param listaClases Lista de objetos ClaseModelo que se mostraran.
 * @param onEliminarClick Funcion que se ejecuta al presionar el boton eliminar.
 * @param onItemClick Funcion que se ejecuta al tocar una clase (para editarla).
 */
class ClaseAdapter(
    private var listaClases: ArrayList<ClaseModelo>,
    private val onEliminarClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ClaseAdapter.ViewHolder>() {


    //  VIEW HOLDER


    /**
     * Clase interna que guarda las referencias a los elementos del diseño
     * item_clase.xml que se usaran para mostrar los datos de cada clase.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreClase)   // Nombre de la clase
        val tvProfesor: TextView = view.findViewById(R.id.tvProfesor)    // Nombre del profesor
        val tvSalon: TextView = view.findViewById(R.id.tvSalon)          // Salon
        val tvHorario: TextView = view.findViewById(R.id.tvHorario)      // Dias y horario
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar) // Boton  eliminar
        val viewColor: View = view.findViewById(R.id.viewColorClase)     // Vista lateral de color
    }


    // -------------------- ADAPTER CORE ----------------------


    /**
     * Crea la vista tarjeta para cada elemento de la lista.
     * Este metodo infla el diseño item_clase.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clase, parent, false)
        return ViewHolder(view)
    }

    /**
     * Asocia los datos de una clase con su vista.
     * También define los eventos para editar o eliminar cada clase.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clase = listaClases[position]

        // Mostramos los datos de la clase
        holder.tvNombre.text = clase.nombre
        holder.tvProfesor.text = "Prof: ${clase.profesor}"
        holder.tvSalon.text = "Salon: ${clase.salon}"
        holder.tvHorario.text = "${clase.dias}\n${clase.horaInicio} - ${clase.horaFin}"


        // Manejo del color de la tarjeta

        val color = try {
            Color.parseColor(clase.color)
        } catch (_: Exception) {
            Color.parseColor("#6200EE") // Color por defecto si ocurre un error
        }

        // Colorea la barra lateral de la tarjeta
        holder.viewColor.setBackgroundColor(color)

        // Colorea el texto del horario con el mismo color
        holder.tvHorario.setTextColor(color)


        // Eventos de clic


        // Al presionar el boton de eliminar
        holder.btnEliminar.setOnClickListener { onEliminarClick(clase.id) }

        // Al tocar la tarjeta completa (para editar)
        holder.itemView.setOnClickListener { onItemClick(clase.id) }
    }

    /**
     * Devuelve el numero total de clases en la lista.
     */
    override fun getItemCount(): Int = listaClases.size




    /**
     * Actualiza la lista mostrada con nuevos datos.
     * Se usa cuando se agregan, editan o eliminan clases.
     *
     * @param nuevaLista Lista actualizada de clases obtenida desde la base de datos.
     */
    fun actualizarLista(nuevaLista: ArrayList<ClaseModelo>) {
        listaClases = nuevaLista
        notifyDataSetChanged() // Refresca visualmente la lista en pantalla
    }
}
