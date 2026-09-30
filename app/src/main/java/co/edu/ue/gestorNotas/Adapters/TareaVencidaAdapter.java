package co.edu.ue.gestorNotas.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.ue.gestorNotas.R;
import co.edu.ue.gestorNotas.Tarea;

/**
 * Adaptador de RecyclerView encargado de vincular los datos de cada Tarea Vencida
 * con los controles del diseño individual (item_tarea_vencida.xml).
 */
public class TareaVencidaAdapter extends RecyclerView.Adapter<TareaVencidaAdapter.TareaVencidaViewHolder> {

    // Interfaz para notificar cuando el usuario presiona el CheckBox de completado
    public interface OnTareaCompletadaListener {
        void onTareaCompletada(Tarea tarea, int posicion);
    }

    private List<Tarea> listaTareasVencidas;
    private OnTareaCompletadaListener listener;

    // Constructor que recibe la lista de datos y el escuchador de eventos
    public TareaVencidaAdapter(List<Tarea> listaTareasVencidas, OnTareaCompletadaListener listener) {
        this.listaTareasVencidas = listaTareasVencidas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TareaVencidaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla la vista de diseño de ítem individual (item_tarea_vencida.xml)
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea_vencida, parent, false);
        return new TareaVencidaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaVencidaViewHolder holder, int position) {
        Tarea tarea = listaTareasVencidas.get(position);

        // Asignación de datos a cada control de texto
        holder.txtNombre.setText(tarea.getNombre());
        holder.txtMateria.setText("Materia: " + tarea.getMateria());
        holder.txtFecha.setText("Venció el: " + tarea.getFecha());

        // Asegurar estado del CheckBox sin disparar escuchadores involuntarios
        holder.chkCompletada.setOnCheckedChangeListener(null);
        holder.chkCompletada.setChecked(tarea.isCompletada());

        // Evento al presionar el CheckBox para marcar la tarea como resuelta
        holder.chkCompletada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTareaCompletada(tarea, adapterPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTareasVencidas.size();
    }

    /**
     * ViewHolder que mantiene las referencias a los componentes gráficos de cada ítem.
     */
    public static class TareaVencidaViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre;
        TextView txtMateria;
        TextView txtFecha;
        CheckBox chkCompletada;

        public TareaVencidaViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombre = itemView.findViewById(R.id.txtNombreTareaVencida);
            txtMateria = itemView.findViewById(R.id.txtMateriaTareaVencida);
            txtFecha = itemView.findViewById(R.id.txtFechaVencida);
            chkCompletada = itemView.findViewById(R.id.chkCompletada);
        }
    }
}
