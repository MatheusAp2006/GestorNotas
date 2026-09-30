package co.edu.ue.gestorNotas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import co.edu.ue.gestorNotas.Database.TareaBDHelper;
import co.edu.ue.gestorNotas.local.AppDatabase;
import co.edu.ue.gestorNotas.local.TareaDao;
import co.edu.ue.gestorNotas.local.TareaEntity;

// Declaración de la clase pública TareasActivity que extiende AppCompatActivity para gestionar las tareas, apuntes e imágenes de una materia usando Room
public class TareasActivity extends AppCompatActivity {

    // Constantes estáticas que definen las opciones de prioridad disponibles
    private static final String[] PRIORIDADES = {"BAJA", "MEDIA", "ALTA"};
    // Constantes estáticas que definen los estados posibles de una tarea
    private static final String[] ESTADOS = {"PENDIENTE", "EN_PROCESO", "FINALIZADO"};

    // Identificador de la materia recibida desde la actividad anterior
    private long idMateria;
    // Interfaz DAO (Data Access Object) de Room para realizar las operaciones de persistencia local en la tabla de tareas
    private TareaDao tareaDao;

    // Componentes RecyclerView para separar las tareas vencidas de las vigentes
    private RecyclerView rvVencidas, rvTareas;
    // Vistas TextView para indicar que los listados correspondientes están vacíos
    private TextView tvVencidasVacio, tvTareasVacio;
    // Adaptadores para gestionar la visualización de las listas de tareas vencidas y normales
    private TareaAdapter adapterVencidas, adapterTareas;

    // Contenedores dinámicos para los apuntes y las imágenes almacenados localmente en SQLite
    private LinearLayout contenedorApuntesMateria;
    private LinearLayout contenedorImagenesMateria;
    // Instancia del helper para la base de datos local SQLite de apuntes e imágenes
    private TareaBDHelper bdHelper;

    // Sobrescribe onCreate para inicializar la actividad y configurar los componentes gráficos
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Vincula el diseño XML 'activity_tareas' con esta vista
        setContentView(R.layout.activity_tareas);

        // Recupera el ID y el nombre de la materia pasados como extras mediante el Intent
        idMateria = getIntent().getLongExtra("idMateria", -1);
        String nombreMateria = getIntent().getStringExtra("materia");

        // Obtiene la instancia singleton del DAO de tareas a través de AppDatabase
        tareaDao = AppDatabase.getInstancia(this).tareaDao();
        // Inicializa la base de datos SQLite local para apuntes/imágenes
        bdHelper = new TareaBDHelper(this);

        // Asigna el nombre de la materia al TextView del encabezado
        TextView txtMateria = findViewById(R.id.txtMateria);
        txtMateria.setText(nombreMateria);

        // Mapea los componentes gráficos con sus respectivos IDs del XML
        rvVencidas = findViewById(R.id.rvVencidas);
        rvTareas = findViewById(R.id.rvTareas);
        tvVencidasVacio = findViewById(R.id.tvVencidasVacio);
        tvTareasVacio = findViewById(R.id.tvTareasVacio);
        contenedorApuntesMateria = findViewById(R.id.contenedorApuntesMateria);
        contenedorImagenesMateria = findViewById(R.id.contenedorImagenesMateria);
        Button btnNueva = findViewById(R.id.btnNuevaTarea);

        // Crea una instancia compartida del listener recibiendo objetos TareaEntity de Room
        TareaAdapter.Listener listener = new TareaAdapter.Listener() {
            @Override
            public void onClick(TareaEntity tarea) {
                // Al hacer clic corto, avanza cíclicamente el estado de la entidad de tarea
                avanzarEstado(tarea);
            }

            @Override
            public void onLongClick(TareaEntity tarea) {
                // Al hacer clic prolongado, despliega el menú de edición/eliminación
                mostrarOpciones(tarea);
            }
        };

        // Inicializa los adaptadores de tareas vencidas y vigentes
        adapterVencidas = new TareaAdapter(listener);
        adapterTareas = new TareaAdapter(listener);
        // Asigna los LayoutManager y adaptadores a sus respectivos RecyclerViews
        rvVencidas.setLayoutManager(new LinearLayoutManager(this));
        rvVencidas.setAdapter(adapterVencidas);
        rvTareas.setLayoutManager(new LinearLayoutManager(this));
        rvTareas.setAdapter(adapterTareas);

        // Configura el evento de clic del botón para crear una nueva tarea mediante diálogo
        btnNueva.setOnClickListener(v -> mostrarDialogoTarea(null));
    }

    // Sobrescribe onResume para recargar las tareas, apuntes e imágenes cada vez que la pantalla vuelva a estar visible
    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas();
        cargarApuntes();
        cargarImagenes();
    }

    // ---------- LEER (Room, en segundo plano) ----------
    // Consulta la base de datos local de Room en un hilo secundario y separa las tareas vencidas de las vigentes
    private void cargarTareas() {
        // Ejecuta la consulta I/O fuera del hilo principal (UI Thread) mediante el executor de AppDatabase
        AppDatabase.executor.execute(() -> {
            // Obtiene la lista de entidades TareaEntity pertenecientes a la materia actual desde Room
            List<TareaEntity> todas = tareaDao.listarPorMateria(idMateria);
            // Obtiene la fecha actual formateada en "yyyy-MM-dd"
            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

            List<TareaEntity> vencidas = new ArrayList<>();
            List<TareaEntity> resto = new ArrayList<>();
            // Clasifica las tareas comprobando si la fecha de entrega caducó respecto a hoy
            for (TareaEntity t : todas) {
                if (t.estaVencida(hoy)) vencidas.add(t);
                else resto.add(t);
            }

            // Regresa al hilo principal para actualizar los elementos de la interfaz de usuario
            runOnUiThread(() -> {
                // Carga las tareas vencidas en su adaptador y ajusta visibilidades
                adapterVencidas.setDatos(vencidas);
                tvVencidasVacio.setVisibility(vencidas.isEmpty() ? View.VISIBLE : View.GONE);
                rvVencidas.setVisibility(vencidas.isEmpty() ? View.GONE : View.VISIBLE);

                // Carga las tareas vigentes en su adaptador y ajusta visibilidades
                adapterTareas.setDatos(resto);
                tvTareasVacio.setVisibility(resto.isEmpty() ? View.VISIBLE : View.GONE);
                rvTareas.setVisibility(resto.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    // ---------- APUNTES E IMAGENES DE LA MATERIA (sin cambios) ----------
    // Obtiene y muestra dinámicamente las imágenes guardadas localmente para esta materia
    private void cargarImagenes() {
        contenedorImagenesMateria.removeAllViews();
        List<String> rutas = bdHelper.obtenerImagenes(idMateria);

        // Si no existen imágenes, muestra un mensaje por defecto
        if (rutas.isEmpty()) {
            contenedorImagenesMateria.addView(crearTextoVacio("Aún no hay imagenes en esta materia"));
            return;
        }

        // Calcula el tamaño en píxeles equivalente a 120dp
        int tamano = (int) (120 * getResources().getDisplayMetrics().density);
        // Itera sobre las rutas, genera ImageView dinámicos con miniaturas optimizadas y los agrega al contenedor
        for (String ruta : rutas) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tamano, tamano);
            params.setMargins(0, 0, 16, 0);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(ImagenUtil.cargarMiniatura(ruta, tamano));
            contenedorImagenesMateria.addView(iv);
        }
    }

    // Obtiene y muestra dinámicamente los apuntes de texto almacenados en SQLite para esta materia
    private void cargarApuntes() {
        contenedorApuntesMateria.removeAllViews();
        List<String> apuntes = bdHelper.obtenerApuntes(idMateria);

        // Si no existen apuntes, muestra un mensaje por defecto
        if (apuntes.isEmpty()) {
            contenedorApuntesMateria.addView(crearTextoVacio("Aún no hay apuntes en esta materia"));
            return;
        }

        // Crea un TextView para cada apunte formateado con viñeta
        for (String apunte : apuntes) {
            TextView tv = new TextView(this);
            tv.setText("• " + apunte);
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 16);
            contenedorApuntesMateria.addView(tv);
        }
    }

    // Método de utilidad para instanciar un TextView estándar con mensaje de contenedor vacío
    private TextView crearTextoVacio(String mensaje) {
        TextView vacio = new TextView(this);
        vacio.setText(mensaje);
        vacio.setTextSize(16);
        return vacio;
    }

    // ---------- CAMBIAR ESTADO (Room) ----------
    // Cambia el estado de la entidad de tarea cíclicamente y lo actualiza en la base de datos de Room
    private void avanzarEstado(TareaEntity tarea) {
        // Obtiene el índice del estado actual y calcula el siguiente usando módulo
        int idx = indiceDe(ESTADOS, tarea.estado);
        tarea.estado = ESTADOS[(idx + 1) % ESTADOS.length];

        // Ejecuta la actualización de la entidad en Room dentro de un hilo en segundo plano
        AppDatabase.executor.execute(() -> {
            tareaDao.actualizar(tarea);
            // Vuelve al hilo de interfaz para recargar los listados en pantalla
            runOnUiThread(this::cargarTareas);
        });
    }

    // ---------- CREAR / EDITAR (Room) ----------
    // Despliega un diálogo emergente para crear o editar una entidad de tarea en Room
    private void mostrarDialogoTarea(TareaEntity existente) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        // Crea dinámicamente los campos de entrada de datos
        EditText etTitulo = new EditText(this);
        etTitulo.setHint("Título de la tarea");
        EditText etDescripcion = new EditText(this);
        etDescripcion.setHint("Descripción (opcional)");
        EditText etFecha = new EditText(this);
        etFecha.setHint("Fecha de entrega AAAA-MM-DD (opcional)");

        // Configura el selector desplegable (Spinner) con las prioridades
        Spinner spPrioridad = new Spinner(this);
        spPrioridad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PRIORIDADES));

        layout.addView(etTitulo);
        layout.addView(etDescripcion);
        layout.addView(etFecha);
        layout.addView(spPrioridad);

        // Si se recibe una tarea existente, carga sus valores directos desde los atributos de TareaEntity
        if (existente != null) {
            etTitulo.setText(existente.titulo);
            etDescripcion.setText(existente.descripcion);
            etFecha.setText(existente.fechaEntrega);
            spPrioridad.setSelection(Math.max(0, indiceDe(PRIORIDADES, existente.prioridad)));
        }

        // Construye y despliega la ventana AlertDialog
        new AlertDialog.Builder(this)
                .setTitle(existente == null ? "Nueva tarea" : "Editar tarea")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String titulo = etTitulo.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    String fecha = etFecha.getText().toString().trim();
                    String prioridad = PRIORIDADES[spPrioridad.getSelectedItemPosition()];

                    // Valida que el título no esté vacío
                    if (titulo.isEmpty()) {
                        mensaje("El título es obligatorio");
                        return;
                    }
                    // Valida que la fecha cumpla con el formato AAAA-MM-DD
                    if (!fecha.isEmpty() && !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        mensaje("La fecha debe tener el formato AAAA-MM-DD");
                        return;
                    }

                    // Ejecuta la inserción o actualización de la entidad en segundo plano
                    AppDatabase.executor.execute(() -> {
                        if (existente == null) {
                            // Instancia una nueva TareaEntity en estado PENDIENTE e inserta vía DAO
                            TareaEntity nueva = new TareaEntity(
                                    idMateria, titulo,
                                    descripcion.isEmpty() ? null : descripcion,
                                    fecha.isEmpty() ? null : fecha,
                                    prioridad, "PENDIENTE");
                            tareaDao.insertar(nueva);
                        } else {
                            // Actualiza los campos de la entidad existente y ejecuta el update vía DAO
                            existente.titulo = titulo;
                            existente.descripcion = descripcion.isEmpty() ? null : descripcion;
                            existente.fechaEntrega = fecha.isEmpty() ? null : fecha;
                            existente.prioridad = prioridad;
                            tareaDao.actualizar(existente);
                        }
                        // Recarga las listas desde el hilo de interfaz
                        runOnUiThread(this::cargarTareas);
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- EDITAR / ELIMINAR ----------
    // Muestra las opciones contextuales para la tarea seleccionada al mantener presionada la tarjeta
    private void mostrarOpciones(TareaEntity tarea) {
        new AlertDialog.Builder(this)
                .setTitle(tarea.titulo)
                .setItems(new String[]{"Editar", "Eliminar"}, (d, which) -> {
                    if (which == 0) mostrarDialogoTarea(tarea);
                    else confirmarEliminar(tarea);
                })
                .show();
    }

    // Muestra una ventana de confirmación antes de eliminar la entidad en Room
    private void confirmarEliminar(TareaEntity tarea) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tarea")
                .setMessage("¿Eliminar \"" + tarea.titulo + "\"?")
                .setPositiveButton("Eliminar", (d, w) ->
                        // Ejecuta el borrado del registro en el hilo executor de Room y refresca la vista
                        AppDatabase.executor.execute(() -> {
                            tareaDao.eliminar(tarea);
                            runOnUiThread(this::cargarTareas);
                        }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- UTILIDADES ----------
    // Busca la posición de una cadena dentro de un arreglo de textos
    private int indiceDe(String[] arreglo, String valor) {
        for (int i = 0; i < arreglo.length; i++) {
            if (arreglo[i].equals(valor)) return i;
        }
        return 0;
    }

    // Emite una notificación Toast en pantalla
    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}