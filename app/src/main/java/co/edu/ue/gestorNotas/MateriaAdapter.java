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

public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.ViewHolder> {

    public interface Listener {
        void onClick(MateriaResponse materia);
        void onLongClick(MateriaResponse materia);
    }

    private final List<MateriaResponse> datos = new ArrayList<>();
    private final Listener listener;

    public MateriaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setDatos(List<MateriaResponse> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MateriaResponse m = datos.get(position);
        holder.tvNombre.setText(m.getNombreMateria());
        String prof = m.getProfesor();
        holder.tvProfesor.setText(prof == null || prof.isEmpty() ? "Sin profesor asignado" : prof);
        holder.itemView.setOnClickListener(v -> listener.onClick(m));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(m);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvProfesor;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvProfesor = itemView.findViewById(R.id.tvProfesor);
        }
    }
}