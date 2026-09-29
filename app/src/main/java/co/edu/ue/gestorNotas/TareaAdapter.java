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

import co.edu.ue.gestorNotas.api.TareaResponse;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    public interface Listener {
        void onClick(TareaResponse tarea);      // tocar: avanza el estado
        void onLongClick(TareaResponse tarea);   // mantener presionado: editar/eliminar
    }

    private final List<TareaResponse> listaTareas = new ArrayList<>();
    private final Listener listener;

    public TareaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setDatos(List<TareaResponse> nuevos) {
        listaTareas.clear();
        listaTareas.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        TareaResponse tarea = listaTareas.get(position);

        holder.txtNombreTarea.setText(tarea.getTitulo());

        String fecha = tarea.getFechaEntrega();
        String prioridad = tarea.getPrioridad();
        holder.txtFechaTarea.setText(
                (fecha != null ? "Entrega: " + fecha : "Sin fecha") + "  ·  " + prioridad);

        holder.txtEstadoTarea.setText(tarea.getEstado());
        holder.txtEstadoTarea.setBackgroundColor(colorEstado(tarea.getEstado()));

        holder.itemView.setOnClickListener(v -> listener.onClick(tarea));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(tarea);
            return true;
        });
    }

    private int colorEstado(String estado) {
        switch (estado) {
            case "FINALIZADO": return Color.parseColor("#2E7D32");
            case "EN_PROCESO": return Color.parseColor("#F9A825");
            default: return Color.parseColor("#757575"); // PENDIENTE
        }
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreTarea;
        TextView txtFechaTarea;
        TextView txtEstadoTarea;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreTarea = itemView.findViewById(R.id.txtNombreTarea);
            txtFechaTarea = itemView.findViewById(R.id.txtFechaTarea);
            txtEstadoTarea = itemView.findViewById(R.id.txtEstadoTarea);
        }
    }
}