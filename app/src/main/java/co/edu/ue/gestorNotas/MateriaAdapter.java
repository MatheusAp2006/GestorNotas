package co.edu.ue.gestorNotas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.ue.gestorNotas.api.MateriaResponse;

// Declaración de la clase pública MateriaAdapter que extiende RecyclerView.Adapter para gestionar la visualización de la lista de materias
public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.ViewHolder> {

    // Declaración de la interfaz interna Listener para comunicar los eventos de clic hacia la actividad o fragmento contenedor
    public interface Listener {
        // Método que se activa al realizar un clic simple sobre una materia
        void onClick(MateriaResponse materia);
        // Método que se activa al realizar un clic largo (prolongado) sobre una materia
        void onLongClick(MateriaResponse materia);
    }

    // Lista interna privada y final que almacena la colección actual de objetos MateriaResponse a mostrar
    private final List<MateriaResponse> datos = new ArrayList<>();
    // Instancia del listener para reenviar las interacciones del usuario
    private final Listener listener;

    // Constructor que recibe la implementación de la interfaz Listener
    public MateriaAdapter(Listener listener) {
        this.listener = listener;
    }

    // Método para reemplazar los datos de la lista y notificar al RecyclerView que refresque la interfaz
    public void setDatos(List<MateriaResponse> nuevos) {
        // Limpia la colección de datos previa
        datos.clear();
        // Agrega todos los elementos recibidos en la nueva lista
        datos.addAll(nuevos);
        // Notifica al adaptador que la fuente de datos ha cambiado por completo
        notifyDataSetChanged();
    }

    // Sobrescribe onCreateViewHolder para inflar la vista XML 'item_materia' y crear la instancia del ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el diseño gráfico individual de cada elemento de la lista desde su archivo de maquetación XML
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        // Crea y retorna una nueva instancia de ViewHolder pasando la vista inflada
        return new ViewHolder(v);
    }

    // Sobrescribe onBindViewHolder para vincular los datos del modelo con los componentes de la vista en una posición dada
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtiene el objeto MateriaResponse correspondiente a la posición actual
        MateriaResponse m = datos.get(position);
        // Asigna el nombre de la materia al TextView del ViewHolder
        holder.tvNombre.setText(m.getNombreMateria());
        // Obtiene el nombre del profesor asignado a la materia
        String prof = m.getProfesor();
        // Valida si el profesor está vacío o es nulo para asignar un texto por defecto o el valor original
        holder.tvProfesor.setText(prof == null || prof.isEmpty() ? "Sin profesor asignado" : prof);
        // Configura el evento OnClick en el ítem completo para notificar la selección simple a la actividad
        holder.itemView.setOnClickListener(v -> listener.onClick(m));
        // Configura el evento OnLongClick en el ítem para activar acciones avanzadas (como editar o eliminar)
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(m);
            return true;
        });
    }

    // Sobrescribe getItemCount para indicar la cantidad total de elementos que contiene la lista
    @Override
    public int getItemCount() {
        return datos.size();
    }

    // Clase interna estática ViewHolder que retiene las referencias a los componentes gráficos de cada ítem
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Declaración de los elementos gráficos dentro del diseño del ítem
        TextView tvNombre;
        TextView tvProfesor;

        // Constructor del ViewHolder que inicializa y enlaza las vistas por sus IDs definidos en el XML
        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvProfesor = itemView.findViewById(R.id.tvProfesor);
        }
    }
}