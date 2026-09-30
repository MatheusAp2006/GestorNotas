package co.edu.ue.gestorNotas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.MateriaResponse;
import co.edu.ue.gestorNotas.api.TareaRequest;
import co.edu.ue.gestorNotas.api.TareaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla (Activity) para la creación de nuevas tareas académicas.
 * Incluye un selector de materias (Spinner), calendario nativo (DatePickerDialog),
 * validaciones estrictas y creación de la tarea en el servidor.
 */
// Declaración de la clase pública NuevaTareaActivity que extiende AppCompatActivity para gestionar la vista de creación de tareas
public class NuevaTareaActivity extends AppCompatActivity {

    // Declaración de los componentes de la interfaz gráfica para captura de datos y botones de acción
    private EditText etTituloTarea;
    private Spinner spnMateriaTarea;
    private EditText etFechaLimite;
    private EditText etDescripcionTarea;
    private Button btnGuardarTarea;
    private Button btnCancelarTarea;
    // Lista local para almacenar los objetos MateriaResponse que poblarán el Spinner
    private final List<MateriaResponse> materias = new ArrayList<>();

    // Sobrescribe el método del ciclo de vida onCreate que se ejecuta al instanciar la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ejecuta la lógica inicial de la clase base AppCompatActivity
        super.onCreate(savedInstanceState);
        // Vincula la vista con el diseño XML definido en 'activity_nueva_tarea'
        setContentView(R.layout.activity_nueva_tarea);

        // PASO 1: Mapea cada variable con su correspondiente ID de componente en el archivo XML
        etTituloTarea = findViewById(R.id.etTituloTarea);
        spnMateriaTarea = findViewById(R.id.spnMateriaTarea);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        etDescripcionTarea = findViewById(R.id.etDescripcionTarea);
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea);
        btnCancelarTarea = findViewById(R.id.btnCancelarTarea);

        // PASO 2: Carga la lista de materias desde el servidor y configura el desplegable (Spinner)
        configurarSpinnerMaterias();

        // PASO 3: Configura el evento de toque en el campo de fecha para mostrar el selector de fecha nativo
        configurarDatePicker();

        // PASO 4: Asigna el evento de clic al botón Guardar invocar las validaciones del formulario
        btnGuardarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYGuardarTarea();
            }
        });

        // PASO 5: Asigna el evento de clic al botón Cancelar para cerrar la pantalla actual sin realizar modificaciones
        btnCancelarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra y destruye la pantalla actual regresando a la actividad anterior
                finish();
            }
        });
    }

    // Método privado que gestiona la solicitud de materias al servidor y llena el Spinner
    private void configurarSpinnerMaterias() {
        // Inicializa el Spinner con la opción por defecto ("Seleccione una materia...")
        actualizarSpinner();

        // Realiza una petición asíncrona mediante Retrofit para listar todas las materias disponibles
        ApiClient.getService().listarMaterias().enqueue(new Callback<List<MateriaResponse>>() {
            @Override
            public void onResponse(Call<List<MateriaResponse>> call, Response<List<MateriaResponse>> response) {
                // Verifica si la llamada fue exitosa (2xx) y posee un cuerpo válido con elementos
                if (response.isSuccessful() && response.body() != null) {
                    // Limpia la colección previa de materias y agrega las obtenidas del backend
                    materias.clear();
                    materias.addAll(response.body());
                    // Vuelve a generar el adaptador con la lista actualizada de materias
                    actualizarSpinner();
                } else {
                    // Notifica en pantalla si ocurrió un error en la respuesta HTTP
                    Toast.makeText(NuevaTareaActivity.this,
                            "Error al cargar materias (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MateriaResponse>> call, Throwable t) {
                // Notifica si no se logró establecer conexión con el backend
                Toast.makeText(NuevaTareaActivity.this,
                        "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Mapea la lista de materias a cadenas de texto e inicializa el ArrayAdapter para el Spinner
    private void actualizarSpinner() {
        // Crea una lista de Strings para contener únicamente los nombres de las materias
        List<String> opcionesSpinner = new ArrayList<>();
        // Agrega un ítem inicial informativo en la posición 0
        opcionesSpinner.add("Seleccione una materia...");
        // Itera sobre las materias y extrae sus nombres
        for (MateriaResponse m : materias) {
            opcionesSpinner.add(m.getNombreMateria());
        }

        // Construye el adaptador del Spinner definiendo el diseño visual de los ítems
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcionesSpinner
        );
        // Aplica el estilo estándar desplegable para las opciones del Spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Asigna el adaptador configurado al componente Spinner
        spnMateriaTarea.setAdapter(adapter);
    }

    // Asigna el selector gráfico de fechas (DatePickerDialog) al campo de entrada de fecha límite
    private void configurarDatePicker() {
        etFechaLimite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instancia un objeto Calendar para tomar la fecha actual del sistema
                final Calendar calendario = Calendar.getInstance();
                int anio = calendario.get(Calendar.YEAR);
                int mes = calendario.get(Calendar.MONTH);
                int dia = calendario.get(Calendar.DAY_OF_MONTH);

                // Construye el cuadro de diálogo flotante con el calendario nativo de Android
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        NuevaTareaActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Formatea el año, mes y día en la cadena con patrón internacional YYYY-MM-DD
                                String fechaFormateada = String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d",
                                        year,
                                        month + 1, // Suma 1 al mes debido a que la API Calendar indexa meses desde 0
                                        dayOfMonth
                                );
                                // Asigna el valor de la fecha formateada en el campo de texto
                                etFechaLimite.setText(fechaFormateada);
                            }
                        },
                        anio, mes, dia
                );

                // Muestra la ventana emergente del calendario en pantalla
                datePickerDialog.show();
            }
        });
    }

    // Extrae y valida las entradas del usuario para enviar la petición de creación de tarea
    private void validarYGuardarTarea() {
        // Obtiene los valores ingresados en los campos de texto y selector
        String titulo = etTituloTarea.getText().toString().trim();
        int posicionMateria = spnMateriaTarea.getSelectedItemPosition();
        String fecha = etFechaLimite.getText().toString().trim();
        String descripcion = etDescripcionTarea.getText().toString().trim();

        // Validacion 1: Verifica que se haya escrito un título para la tarea
        if (titulo.isEmpty()) {
            etTituloTarea.setError("Requerido");
            etTituloTarea.requestFocus();
            return;
        }

        // Validacion 2: Verifica que la opción seleccionada no sea el mensaje por defecto (índice 0)
        if (posicionMateria <= 0) {
            Toast.makeText(this, "Por favor seleccione una materia válida", Toast.LENGTH_SHORT).show();
            spnMateriaTarea.requestFocus();
            return;
        }
        // Obtiene el objeto MateriaResponse mapeado restando 1 al índice por el título informativo
        MateriaResponse materia = materias.get(posicionMateria - 1);

        // Validacion 3: Verifica que se haya seleccionado una fecha límite
        if (fecha.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar una fecha límite con el calendario", Toast.LENGTH_SHORT).show();
            etFechaLimite.requestFocus();
            return;
        }

        // Bloquea temporalmente el botón de guardar para evitar clics duplicados
        btnGuardarTarea.setEnabled(false);

        // Instancia la solicitud DTO TareaRequest asignando ID, título, descripción, fecha, prioridad "MEDIA" y estado "PENDIENTE"
        TareaRequest req = new TareaRequest(
                materia.getId(), titulo,
                descripcion.isEmpty() ? null : descripcion,
                fecha, "MEDIA", "PENDIENTE");

        // Ejecuta la llamada asíncrona hacia el servicio REST para guardar la tarea
        ApiClient.getService().crearTarea(req).enqueue(new Callback<TareaResponse>() {
            @Override
            public void onResponse(Call<TareaResponse> call, Response<TareaResponse> response) {
                // Habilita nuevamente el botón al recibir respuesta del servidor
                btnGuardarTarea.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(NuevaTareaActivity.this, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra el formulario finalizando la actividad
                } else {
                    Toast.makeText(NuevaTareaActivity.this,
                            "Error al guardar la tarea (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TareaResponse> call, Throwable t) {
                // Reactiva el botón en caso de error de conexión y emite la alerta
                btnGuardarTarea.setEnabled(true);
                Toast.makeText(NuevaTareaActivity.this,
                        "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}