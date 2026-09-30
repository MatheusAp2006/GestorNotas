package co.edu.ue.gestorNotas;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.ue.gestorNotas.local.TareaEntity;

// Declaración de la clase pública TareaAdapter que extiende RecyclerView.Adapter para poblar el listado de tareas basadas en entidades Room
public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    // Interfaz pública para capturar e interpretar eventos de clic simple y largo sobre cada ítem usando TareaEntity
    public interface Listener {
        // Evento que se activa al presionar brevemente la tarea para avanzar de estado (PENDIENTE -> EN_PROCESO -> FINALIZADO)
        void onClick(TareaEntity tarea);
        // Evento que se activa al mantener presionada la tarea para desplegar opciones secundarias (editar/eliminar)
        void onLongClick(TareaEntity tarea);
    }

    // Lista interna privada que almacena la colección actual de objetos TareaEntity provenientes de Room
    private final List<TareaEntity> listaTareas = new ArrayList<>();
    // Instancia del listener que gestiona las acciones desencadenadas por el usuario
    private final Listener listener;

    // Constructor de la clase que recibe la implementación de la interfaz Listener
    public TareaAdapter(Listener listener) {
        this.listener = listener;
    }

    // Método que actualiza los elementos de la lista y notifica al RecyclerView para re-renderizar la vista
    public void setDatos(List<TareaEntity> nuevos) {
        // Vacía la colección de datos previa
        listaTareas.clear();
        // Incorpora todos los elementos de la lista actualizada de entidades Room
        listaTareas.addAll(nuevos);
        // Notifica al adaptador que el conjunto de datos cambió para refrescar la interfaz
        notifyDataSetChanged();
    }

    // Sobrescribe onCreateViewHolder para inflar el diseño XML 'item_tarea' e instanciar el ViewHolder
    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla la vista de cada tarjeta/ítem de tarea desde la maquetación del archivo XML correspondiente
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        // Instancia y retorna un nuevo TareaViewHolder pasando la vista inflada
        return new TareaViewHolder(vista);
    }

    // Sobrescribe onBindViewHolder para mapear los atributos de TareaEntity con las vistas del ViewHolder en la posición indicada
    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        // Obtiene el objeto TareaEntity de la lista según la posición solicitada
        TareaEntity tarea = listaTareas.get(position);

        // Asigna el título de la tarea desde el atributo público 'titulo' de TareaEntity
        holder.txtNombreTarea.setText(tarea.titulo);
        // Formatea y concatena la fecha de entrega y la prioridad registradas en la entidad
        holder.txtFechaTarea.setText(
                (tarea.fechaEntrega != null ? "Entrega: " + tarea.fechaEntrega : "Sin fecha")
                        + "  ·  " + tarea.prioridad);

        // Si la descripción contiene texto, la asigna y activa la visibilidad del TextView
        if (tarea.descripcion != null && !tarea.descripcion.isEmpty()) {
            holder.txtDescripcionTarea.setText(tarea.descripcion);
            holder.txtDescripcionTarea.setVisibility(View.VISIBLE);
        } else {
            // Oculta el campo de descripción si viene nulo o vacío para no dejar espacios en blanco inútiles
            holder.txtDescripcionTarea.setVisibility(View.GONE);
        }

        // Asigna la cadena de texto del estado actual (ej. "PENDIENTE", "EN_PROCESO", "FINALIZADO")
        holder.txtEstadoTarea.setText(tarea.estado);
        // Establece el color de fondo de la etiqueta de estado calculándolo dinámicamente según su valor
        holder.txtEstadoTarea.setBackgroundColor(colorEstado(tarea.estado));

        // Registra el evento de clic simple en el contenedor del ítem para cambiar o avanzar el estado de la tarea
        holder.itemView.setOnClickListener(v -> listener.onClick(tarea));
        // Registra el evento de clic prolongado para desplegar las opciones de edición o eliminación
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(tarea);
            return true;
        });
    }

    // Método helper privado que retorna un color hexadecimal (convertido a entero) según el estado de la tarea
    private int colorEstado(String estado) {
        switch (estado) {
            // Tarea completada: asigna verde (#2E7D32)
            case "FINALIZADO": return Color.parseColor("#2E7D32");
            // Tarea en curso: asigna amarillo/naranja (#F9A825)
            case "EN_PROCESO": return Color.parseColor("#F9A825");
            // Tarea pendiente o cualquier otro valor: asigna gris (#757575) por defecto
            default: return Color.parseColor("#757575");
        }
    }

    // Sobrescribe getItemCount para obtener el total de elementos cargados dentro de la lista
    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    // Clase interna estática TareaViewHolder que retiene las referencias a los elementos gráficos de la tarjeta
    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        // Declaración de las referencias visuales dentro del ítem de tarea
        TextView txtNombreTarea, txtFechaTarea, txtDescripcionTarea, txtEstadoTarea;

        // Constructor del ViewHolder que vincula los componentes por sus respectivos IDs en el XML
        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreTarea = itemView.findViewById(R.id.txtNombreTarea);
            txtFechaTarea = itemView.findViewById(R.id.txtFechaTarea);
            txtDescripcionTarea = itemView.findViewById(R.id.txtDescripcionTarea);
            txtEstadoTarea = itemView.findViewById(R.id.txtEstadoTarea);
        }
    }
}