package co.edu.ue.gestorNotas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.MateriaRequest;
import co.edu.ue.gestorNotas.api.MateriaResponse;
import co.edu.ue.gestorNotas.api.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioActivity extends AppCompatActivity {

    private RecyclerView rvMaterias;
    private TextView tvVacio;
    private MateriaAdapter adapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        session = new SessionManager(this);
        rvMaterias = findViewById(R.id.rvMaterias);
        tvVacio = findViewById(R.id.tvVacio);
        Button btnNueva = findViewById(R.id.btnNuevaMateria);
        Button btnCerrar = findViewById(R.id.btnCerrarSesion);

        adapter = new MateriaAdapter(new MateriaAdapter.Listener() {
            @Override
            public void onClick(MateriaResponse materia) {
                abrirTareas(materia);
            }

            @Override
            public void onLongClick(MateriaResponse materia) {
                mostrarOpciones(materia);
            }
        });
        rvMaterias.setLayoutManager(new LinearLayoutManager(this));
        rvMaterias.setAdapter(adapter);

        btnNueva.setOnClickListener(v -> mostrarDialogoMateria(null));
        btnCerrar.setOnClickListener(v -> cerrarSesion());
    }

    // Se recarga cada vez que la pantalla vuelve a ser visible
    @Override
    protected void onResume() {
        super.onResume();
        cargarMaterias();
    }

    // ---------- LEER ----------
    private void cargarMaterias() {
        ApiClient.getService().listarMaterias().enqueue(new Callback<List<MateriaResponse>>() {
            @Override
            public void onResponse(Call<List<MateriaResponse>> call, Response<List<MateriaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setDatos(response.body());
                    tvVacio.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else if (response.code() == 401) {
                    // Token vencido o inválido: se vuelve al login
                    cerrarSesion();
                } else {
                    mensaje("Error al cargar materias (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<MateriaResponse>> call, Throwable t) {
                mensaje("No se pudo conectar con el servidor");
            }
        });
    }

    // ---------- CREAR / EDITAR ----------
    private void mostrarDialogoMateria(MateriaResponse existente) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre de la materia");
        EditText etProfesor = new EditText(this);
        etProfesor.setHint("Profesor (opcional)");
        layout.addView(etNombre);
        layout.addView(etProfesor);

        if (existente != null) {
            etNombre.setText(existente.getNombreMateria());
            etProfesor.setText(existente.getProfesor());
        }

        new AlertDialog.Builder(this)
                .setTitle(existente == null ? "Nueva materia" : "Editar materia")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String profesor = etProfesor.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        mensaje("El nombre es obligatorio");
                        return;
                    }
                    MateriaRequest req = new MateriaRequest(
                            nombre,
                            profesor.isEmpty() ? null : profesor,
                            existente != null ? existente.getColorHex() : null);
                    if (existente == null) {
                        ApiClient.getService().crearMateria(req)
                                .enqueue(alTerminar("No se pudo crear la materia"));
                    } else {
                        ApiClient.getService().actualizarMateria(existente.getId(), req)
                                .enqueue(alTerminar("No se pudo actualizar la materia"));
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- EDITAR / ELIMINAR (mantener presionado) ----------
    private void mostrarOpciones(MateriaResponse materia) {
        new AlertDialog.Builder(this)
                .setTitle(materia.getNombreMateria())
                .setItems(new String[]{"Editar", "Eliminar"}, (d, which) -> {
                    if (which == 0) {
                        mostrarDialogoMateria(materia);
                    } else {
                        confirmarEliminar(materia);
                    }
                })
                .show();
    }

    private void confirmarEliminar(MateriaResponse materia) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar materia")
                .setMessage("¿Eliminar \"" + materia.getNombreMateria() + "\"? También se eliminarán sus tareas.")
                .setPositiveButton("Eliminar", (d, w) ->
                        ApiClient.getService().eliminarMateria(materia.getId())
                                .enqueue(alTerminar("No se pudo eliminar la materia")))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- UTILIDADES ----------
    // Callback reutilizable: si sale bien recarga la lista, si no muestra el error
    private <T> Callback<T> alTerminar(String mensajeError) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    cargarMaterias();
                } else {
                    mensaje(mensajeError + " (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                mensaje("No se pudo conectar con el servidor");
            }
        };
    }

    private void abrirTareas(MateriaResponse materia) {
        Intent intent = new Intent(this, TareasActivity.class);
        intent.putExtra("idMateria", materia.getId());
        intent.putExtra("materia", materia.getNombreMateria());
        startActivity(intent);
    }

    private void cerrarSesion() {
        session.cerrarSesion();
        ApiClient.setToken(null);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}