package co.edu.ue.gestorNotas;

import android.content.Intent;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.MateriaRequest;
import co.edu.ue.gestorNotas.api.MateriaResponse;
import co.edu.ue.gestorNotas.api.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Declaración de la clase pública InicioActivity que extiende AppCompatActivity para gestionar la pantalla principal de materias
public class InicioActivity extends AppCompatActivity {

    // Componentes de la interfaz gráfica: RecyclerView para el listado y TextView para el mensaje de lista vacía
    private RecyclerView rvMaterias;
    private TextView tvVacio;
    // Adaptador para gestionar el renderizado de la lista de materias en el RecyclerView
    private MateriaAdapter adapter;
    // Gestor de sesión preferencial para administrar las preferencias guardadas del usuario
    private SessionManager session;
    // Lista local que almacena los objetos MateriaResponse traídos del backend
    private final List<MateriaResponse> materias = new ArrayList<>();

    // Sobrescribe el método del ciclo de vida onCreate para inicializar la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llama a la implementación base del método onCreate
        super.onCreate(savedInstanceState);
        // Establece el archivo de maquetación XML correspondiente a la actividad de inicio
        setContentView(R.layout.activity_inicio);

        // Inicializa el administrador de sesión
        session = new SessionManager(this);
        // Vincula los componentes de la vista con sus IDs definidos en el XML
        rvMaterias = findViewById(R.id.rvMaterias);
        tvVacio = findViewById(R.id.tvVacio);
        Button btnNueva = findViewById(R.id.btnNuevaMateria);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarTarea);
        Button btnTareasVencidas = findViewById(R.id.btnTareasVencidas);
        Button btnAgregarApuntes = findViewById(R.id.btnAgregarApuntes);
        Button btnCerrar = findViewById(R.id.btnCerrarSesion);

        // Inicializa el adaptador implementando sus listeners para eventos de clic normal y clic prolongado
        adapter = new MateriaAdapter(new MateriaAdapter.Listener() {
            @Override
            public void onClick(MateriaResponse materia) {
                // Abre la vista de tareas pertenecientes a la materia seleccionada
                abrirTareas(materia);
            }

            @Override
            public void onLongClick(MateriaResponse materia) {
                // Muestra el menú de opciones (Editar/Eliminar) de la materia seleccionada
                mostrarOpciones(materia);
            }
        });
        // Configura el gestor de diseño lineal en el RecyclerView
        rvMaterias.setLayoutManager(new LinearLayoutManager(this));
        // Asigna el adaptador configurado al RecyclerView
        rvMaterias.setAdapter(adapter);

        // Asigna el evento de clic al botón de agregar nueva materia
        btnNueva.setOnClickListener(v -> mostrarDialogoMateria(null));
        // Asigna el evento de clic al botón para navegar a la creación de una nueva tarea
        btnAgregarTarea.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, NuevaTareaActivity.class)));
        // Asigna el evento de clic al botón para navegar a la vista de tareas vencidas
        btnTareasVencidas.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, TareasVencidasActivity.class)));
        // Asigna el evento de clic al botón para abrir el selector de materias y ver/añadir apuntes
        btnAgregarApuntes.setOnClickListener(v -> mostrarSelectorMateriaParaApuntes());
        // Asigna el evento de clic al botón para cerrar la sesión actual
        btnCerrar.setOnClickListener(v -> cerrarSesion());
    }

    // Sobrescribe el método del ciclo de vida onResume que se ejecuta cada vez que la pantalla vuelve a primer plano
    @Override
    protected void onResume() {
        super.onResume();
        // Carga la lista actualizada de materias desde la API REST
        cargarMaterias();
    }

    // ---------- LEER ----------
    // Consulta al backend para listar las materias asociadas al usuario autenticado
    private void cargarMaterias() {
        ApiClient.getService().listarMaterias().enqueue(new Callback<List<MateriaResponse>>() {
            @Override
            public void onResponse(Call<List<MateriaResponse>> call, Response<List<MateriaResponse>> response) {
                // Verifica si la respuesta HTTP es exitosa y contiene datos
                if (response.isSuccessful() && response.body() != null) {
                    // Limpia la lista local e inserta los datos recibidos
                    materias.clear();
                    materias.addAll(response.body());
                    // Actualiza los datos dentro del adaptador del RecyclerView
                    adapter.setDatos(response.body());
                    // Alterna la visibilidad del mensaje de lista vacía
                    tvVacio.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else if (response.code() == 401) {
                    // Si la API responde con no autorizado (401), invalida la sesión y fuerza el re-login
                    cerrarSesion();
                } else {
                    // Muestra una notificación con el código de error correspondiente
                    mensaje("Error al cargar materias (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<MateriaResponse>> call, Throwable t) {
                // Notifica al usuario en caso de fallos de red o falta de conexión
                mensaje("No se pudo conectar con el servidor");
            }
        });
    }

    // ---------- CREAR / EDITAR ----------
    // Despliega un cuadro de diálogo dinámico para registrar una materia o modificar una existente
    private void mostrarDialogoMateria(MateriaResponse existente) {
        // Crea un contenedor lineal vertical mediante código para incluir en el diálogo
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        // Calcula el espaciado dinámico en píxeles basado en la densidad de la pantalla
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        // Instancia los campos de entrada para el nombre de la materia y el profesor
        EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre de la materia");
        EditText etProfesor = new EditText(this);
        etProfesor.setHint("Profesor (opcional)");
        layout.addView(etNombre);
        layout.addView(etProfesor);

        // Si se recibe un objeto existente, rellena los campos con sus datos actuales para edición
        if (existente != null) {
            etNombre.setText(existente.getNombreMateria());
            etProfesor.setText(existente.getProfesor());
        }

        // Construye y muestra la ventana emergenteAlertDialog
        new AlertDialog.Builder(this)
                .setTitle(existente == null ? "Nueva materia" : "Editar materia")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    // Obtiene las cadenas ingresadas eliminando espacios sobrantes
                    String nombre = etNombre.getText().toString().trim();
                    String profesor = etProfesor.getText().toString().trim();
                    // Valida que el nombre de la materia no esté vacío
                    if (nombre.isEmpty()) {
                        mensaje("El nombre es obligatorio");
                        return;
                    }
                    // Valida que no exista una materia duplicada con el mismo nombre
                    if (existeMateria(nombre, existente)) {
                        mensaje("Esta materia ya está registrada");
                        return;
                    }
                    // Construye el objeto de solicitud HTTP con los datos procesados
                    MateriaRequest req = new MateriaRequest(
                            nombre,
                            profesor.isEmpty() ? null : profesor,
                            existente != null ? existente.getColorHex() : null);
                    // Decide si realiza la llamada para crear o para actualizar
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
    // Muestra un menú de opciones emergente al mantener presionada una materia
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

    // Despliega una confirmación antes de eliminar permanentemente la materia y sus tareas asociadas
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
    // Método genérico que construye un Callback de Retrofit reutilizable para operaciones de mutación
    private <T> Callback<T> alTerminar(String mensajeError) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    // Si la operación concluyó con éxito, recarga el listado completo
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

    // Comprueba si ya existe una materia registrada con el mismo nombre, ignorando diferencias de mayúsculas
    private boolean existeMateria(String nombre, MateriaResponse excluida) {
        for (MateriaResponse m : materias) {
            // Ignora la materia actual si se está editando
            if (excluida != null && m.getId() == excluida.getId()) continue;
            if (m.getNombreMateria().trim().equalsIgnoreCase(nombre.trim())) return true;
        }
        return false;
    }

    // Despliega un selector en diálogo para elegir la materia a la que se vincularán apuntes
    private void mostrarSelectorMateriaParaApuntes() {
        if (materias.isEmpty()) {
            mensaje("Aún no tienes materias. Crea una primero.");
            return;
        }
        final List<MateriaResponse> disponibles = new ArrayList<>(materias);
        final String[] nombres = new String[disponibles.size()];
        for (int i = 0; i < nombres.length; i++) {
            nombres[i] = disponibles.get(i).getNombreMateria();
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona una materia")
                .setItems(nombres, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MateriaResponse elegida = disponibles.get(which);
                        // Navega a la pantalla de Apuntes llevando el ID y el nombre de la materia seleccionada
                        Intent intent = new Intent(InicioActivity.this, ApuntesMateriaActivity.class);
                        intent.putExtra("idMateria", elegida.getId());
                        intent.putExtra("materia", elegida.getNombreMateria());
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Inicia la actividad TareasActivity pasando la materia elegida como argumento
    private void abrirTareas(MateriaResponse materia) {
        Intent intent = new Intent(this, TareasActivity.class);
        intent.putExtra("idMateria", materia.getId());
        intent.putExtra("materia", materia.getNombreMateria());
        startActivity(intent);
    }

    // Limpia la información guardada en la sesión, borra el token global y redirige al inicio de sesión (MainActivity)
    private void cerrarSesion() {
        session.cerrarSesion();
        ApiClient.setToken(null);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Imprime un mensaje informativo mediante un Toast en pantalla
    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}