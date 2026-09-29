package co.edu.ue.gestorNotas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.EstadoRequest;
import co.edu.ue.gestorNotas.api.TareaRequest;
import co.edu.ue.gestorNotas.api.TareaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TareasActivity extends AppCompatActivity {

    private static final String[] PRIORIDADES = {"BAJA", "MEDIA", "ALTA"};
    private static final String[] ESTADOS = {"PENDIENTE", "EN_PROCESO", "FINALIZADO"};

    private long idMateria;
    private RecyclerView rvVencidas, rvTareas;
    private TextView tvVencidasVacio, tvTareasVacio;
    private TareaAdapter adapterVencidas, adapterTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tareas);

        idMateria = getIntent().getLongExtra("idMateria", -1);
        String nombreMateria = getIntent().getStringExtra("materia");

        TextView txtMateria = findViewById(R.id.txtMateria);
        txtMateria.setText(nombreMateria);

        rvVencidas = findViewById(R.id.rvVencidas);
        rvTareas = findViewById(R.id.rvTareas);
        tvVencidasVacio = findViewById(R.id.tvVencidasVacio);
        tvTareasVacio = findViewById(R.id.tvTareasVacio);
        Button btnNueva = findViewById(R.id.btnNuevaTarea);

        TareaAdapter.Listener listener = new TareaAdapter.Listener() {
            @Override
            public void onClick(TareaResponse tarea) {
                avanzarEstado(tarea);
            }

            @Override
            public void onLongClick(TareaResponse tarea) {
                mostrarOpciones(tarea);
            }
        };

        adapterVencidas = new TareaAdapter(listener);
        adapterTareas = new TareaAdapter(listener);

        rvVencidas.setLayoutManager(new LinearLayoutManager(this));
        rvVencidas.setAdapter(adapterVencidas);
        rvTareas.setLayoutManager(new LinearLayoutManager(this));
        rvTareas.setAdapter(adapterTareas);

        btnNueva.setOnClickListener(v -> mostrarDialogoTarea(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas();
    }

    // ---------- LEER Y SEPARAR VENCIDAS ----------
    private void cargarTareas() {
        ApiClient.getService().listarTareas(idMateria).enqueue(new Callback<List<TareaResponse>>() {
            @Override
            public void onResponse(Call<List<TareaResponse>> call, Response<List<TareaResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    mensaje("Error al cargar tareas (" + response.code() + ")");
                    return;
                }
                String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

                List<TareaResponse> vencidas = new ArrayList<>();
                List<TareaResponse> resto = new ArrayList<>();
                for (TareaResponse t : response.body()) {
                    if (t.estaVencida(hoy)) vencidas.add(t);
                    else resto.add(t);
                }

                adapterVencidas.setDatos(vencidas);
                tvVencidasVacio.setVisibility(vencidas.isEmpty() ? View.VISIBLE : View.GONE);
                rvVencidas.setVisibility(vencidas.isEmpty() ? View.GONE : View.VISIBLE);

                adapterTareas.setDatos(resto);
                tvTareasVacio.setVisibility(resto.isEmpty() ? View.VISIBLE : View.GONE);
                rvTareas.setVisibility(resto.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(Call<List<TareaResponse>> call, Throwable t) {
                mensaje("No se pudo conectar con el servidor");
            }
        });
    }

    // ---------- CAMBIAR ESTADO (tocar la tarea) ----------
    private void avanzarEstado(TareaResponse tarea) {
        int idx = indiceDe(ESTADOS, tarea.getEstado());
        String nuevoEstado = ESTADOS[(idx + 1) % ESTADOS.length];

        ApiClient.getService().cambiarEstado(tarea.getId(), new EstadoRequest(nuevoEstado))
                .enqueue(new Callback<TareaResponse>() {
                    @Override
                    public void onResponse(Call<TareaResponse> call, Response<TareaResponse> response) {
                        if (response.isSuccessful()) {
                            cargarTareas();
                        } else {
                            mensaje("No se pudo cambiar el estado (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<TareaResponse> call, Throwable t) {
                        mensaje("No se pudo conectar con el servidor");
                    }
                });
    }

    // ---------- CREAR / EDITAR ----------
    private void mostrarDialogoTarea(TareaResponse existente) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText etTitulo = new EditText(this);
        etTitulo.setHint("Título de la tarea");
        EditText etDescripcion = new EditText(this);
        etDescripcion.setHint("Descripción (opcional)");
        EditText etFecha = new EditText(this);
        etFecha.setHint("Fecha de entrega AAAA-MM-DD (opcional)");

        Spinner spPrioridad = new Spinner(this);
        spPrioridad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PRIORIDADES));

        layout.addView(etTitulo);
        layout.addView(etDescripcion);
        layout.addView(etFecha);
        layout.addView(spPrioridad);

        if (existente != null) {
            etTitulo.setText(existente.getTitulo());
            etDescripcion.setText(existente.getDescripcion());
            etFecha.setText(existente.getFechaEntrega());
            spPrioridad.setSelection(Math.max(0, indiceDe(PRIORIDADES, existente.getPrioridad())));
        }

        new AlertDialog.Builder(this)
                .setTitle(existente == null ? "Nueva tarea" : "Editar tarea")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String titulo = etTitulo.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    String fecha = etFecha.getText().toString().trim();
                    String prioridad = PRIORIDADES[spPrioridad.getSelectedItemPosition()];

                    if (titulo.isEmpty()) {
                        mensaje("El título es obligatorio");
                        return;
                    }
                    if (!fecha.isEmpty() && !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        mensaje("La fecha debe tener el formato AAAA-MM-DD");
                        return;
                    }

                    String estado = existente != null ? existente.getEstado() : "PENDIENTE";
                    TareaRequest req = new TareaRequest(
                            idMateria, titulo,
                            descripcion.isEmpty() ? null : descripcion,
                            fecha.isEmpty() ? null : fecha,
                            prioridad, estado);

                    if (existente == null) {
                        ApiClient.getService().crearTarea(req).enqueue(alTerminar("No se pudo crear la tarea"));
                    } else {
                        ApiClient.getService().actualizarTarea(existente.getId(), req)
                                .enqueue(alTerminar("No se pudo actualizar la tarea"));
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- EDITAR / ELIMINAR (mantener presionado) ----------
    private void mostrarOpciones(TareaResponse tarea) {
        new AlertDialog.Builder(this)
                .setTitle(tarea.getTitulo())
                .setItems(new String[]{"Editar", "Eliminar"}, (d, which) -> {
                    if (which == 0) mostrarDialogoTarea(tarea);
                    else confirmarEliminar(tarea);
                })
                .show();
    }

    private void confirmarEliminar(TareaResponse tarea) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tarea")
                .setMessage("¿Eliminar \"" + tarea.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (d, w) ->
                        ApiClient.getService().eliminarTarea(tarea.getId())
                                .enqueue(alTerminar("No se pudo eliminar la tarea")))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- UTILIDADES ----------
    private <T> Callback<T> alTerminar(String mensajeError) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) cargarTareas();
                else mensaje(mensajeError + " (" + response.code() + ")");
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                mensaje("No se pudo conectar con el servidor");
            }
        };
    }

    private int indiceDe(String[] arreglo, String valor) {
        for (int i = 0; i < arreglo.length; i++) {
            if (arreglo[i].equals(valor)) return i;
        }
        return 0;
    }

    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}