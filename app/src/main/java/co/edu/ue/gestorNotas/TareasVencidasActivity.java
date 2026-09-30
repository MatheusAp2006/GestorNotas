package co.edu.ue.gestorNotas;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.ue.gestorNotas.Adapters.TareaVencidaAdapter;
import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.EstadoRequest;
import co.edu.ue.gestorNotas.api.MateriaResponse;
import co.edu.ue.gestorNotas.api.TareaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla (Activity) encargada de consultar y desplegar las tareas cuyo estado
 * sea pendiente y su fecha límite sea menor a la fecha actual del dispositivo.
 */
// Declaración de la clase pública TareasVencidasActivity que extiende AppCompatActivity para gestionar la consulta y resolución de tareas vencidas
public class TareasVencidasActivity extends AppCompatActivity {

    // Declaración de los componentes gráficos de la interfaz: botón de regreso, contadores de texto y la lista
    private ImageButton btnVolver;
    private TextView txtTotalTareasVencidas;
    private TextView txtSinTareasVencidas;
    private RecyclerView rvTareasVencidas;

    // Instancia del adaptador específico para tareas vencidas
    private TareaVencidaAdapter adapter;
    // Colección local de objetos Tarea que contienen la información agregada de tareas en estado vencido
    private List<Tarea> listaTareasVencidas;

    // Sobrescribe el método del ciclo de vida onCreate para instanciar e inicializar la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llama al método base onCreate de la clase padre AppCompatActivity
        super.onCreate(savedInstanceState);
        // Vincula el diseño XML 'activity_tareas_vencidas' con esta pantalla
        setContentView(R.layout.activity_tareas_vencidas);

        // PASO 1: Enlaza las referencias locales con los componentes definidos en el XML usando sus IDs
        btnVolver = findViewById(R.id.btnVolver);
        txtTotalTareasVencidas = findViewById(R.id.txtTotalTareasVencidas);
        txtSinTareasVencidas = findViewById(R.id.txtSinTareasVencidas);
        rvTareasVencidas = findViewById(R.id.rvTareasVencidas);

        // Asigna un LinearLayoutManager vertical al RecyclerView para acomodar los ítems en lista
        rvTareasVencidas.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa la lista en memoria como un ArrayList vacío
        listaTareasVencidas = new ArrayList<>();

        // PASO 2: Configura el adaptador y le define las acciones al resolver/completar una tarea vencida
        configurarAdaptador();

        // PASO 3: Evalúa el estado actual de la lista para mostrar u ocultar el mensaje de vista vacía
        actualizarEstadoLista();

        // PASO 4: Asigna el evento OnClick al botón de retorno para cerrar esta actividad y volver a la anterior
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finaliza la pantalla actual eliminándola de la pila de actividades
                finish();
            }
        });
    }

    // Sobrescribe el método onResume que se ejecuta al mostrar o regresar a esta pantalla
    @Override
    protected void onResume() {
        super.onResume();
        // Consulta las tareas vencidas desde el servidor backend
        recargarListaTareas();
    }

    // Obtiene la fecha del sistema en formato 'yyyy-MM-dd' e inicia la consulta de materias
    private void recargarListaTareas() {
        // Obtiene y da formato a la fecha del día de hoy acorde a la configuración regional del dispositivo
        final String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Consulta de forma asíncrona la lista de materias disponibles al backend
        ApiClient.getService().listarMaterias().enqueue(new Callback<List<MateriaResponse>>() {
            @Override
            public void onResponse(Call<List<MateriaResponse>> call, Response<List<MateriaResponse>> response) {
                // Comprueba si la respuesta del servidor no fue exitosa o retornó un cuerpo nulo
                if (!response.isSuccessful() || response.body() != null) {
                    mensaje("Error al cargar tareas (" + response.code() + ")");
                    return;
                }
                // Si la consulta fue exitosa, invoca la recolección de tareas vencidas materia por materia
                cargarVencidasDe(response.body(), fechaActual);
            }

            @Override
            public void onFailure(Call<List<MateriaResponse>> call, Throwable t) {
                // Muestra un aviso en pantalla si la petición HTTP falló por problemas de red
                mensaje("No se pudo conectar con el servidor");
            }
        });
    }

    // Recorre cada materia para obtener sus tareas e identificar de forma agregada cuáles están vencidas
    private void cargarVencidasDe(final List<MateriaResponse> materias, final String fechaActual) {
        // Lista temporal para recopilar las tareas vencidas halladas en todas las materias
        final List<Tarea> acumuladas = new ArrayList<>();

        // Si el usuario no tiene materias registradas, muestra inmediatamente el listado vacío
        if (materias.isEmpty()) {
            mostrarVencidas(acumuladas);
            return;
        }

        // Contador de solicitudes pendientes para controlar la asincronía de múltiples peticiones HTTP
        final int[] pendientes = {materias.size()};
        // Bandera de control para detectar si ocurrió un fallo en alguna de las peticiones
        final boolean[] huboError = {false};

        // Itera sobre la lista de materias para consultar las tareas de cada una de ellas de manera individual
        for (final MateriaResponse materia : materias) {
            ApiClient.getService().listarTareas(materia.getId()).enqueue(new Callback<List<TareaResponse>>() {
                @Override
                public void onResponse(Call<List<TareaResponse>> call, Response<List<TareaResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Revisa cada tarea de la materia e identifica si se encuentra vencida respecto a la fecha actual
                        for (TareaResponse t : response.body()) {
                            if (t.estaVencida(fechaActual)) {
                                // Convierte TareaResponse al objeto de modelo local Tarea y lo agrega a la lista acumulada
                                acumuladas.add(new Tarea(t.getId(), t.getTitulo(), materia.getNombreMateria(),
                                        t.getFechaEntrega(), t.getDescripcion(), false));
                            }
                        }
                    } else {
                        huboError[0] = true;
                    }
                    // Decrementa el contador de llamadas pendientes y verifica si fue la última en terminar
                    terminarConsulta(pendientes, huboError, acumuladas);
                }

                @Override
                public void onFailure(Call<List<TareaResponse>> call, Throwable t) {
                    huboError[0] = true;
                    // Decrementa el contador de llamadas pendientes en caso de error de red
                    terminarConsulta(pendientes, huboError, acumuladas);
                }
            });
        }
    }

    // Controla la finalización de la iteración asíncrona reduciendo el contador de consultas pendientes
    private void terminarConsulta(int[] pendientes, boolean[] huboError, List<Tarea> acumuladas) {
        pendientes[0]--;
        // Si aún faltan materias por responder, detiene la ejecución
        if (pendientes[0] > 0) return;

        // Si se registró algún error durante las peticiones, muestra una notificación
        if (huboError[0]) {
            mensaje("No se pudieron cargar todas las tareas");
        }
        // Despliega la lista acumulada de tareas vencidas
        mostrarVencidas(acumuladas);
    }

    // Ordena las tareas vencidas cronológicamente por su fecha de entrega y refresca la interfaz
    private void mostrarVencidas(List<Tarea> tareas) {
        // Ordena la lista de tareas acumuladas comparando la cadena de fecha (YYYY-MM-DD)
        Collections.sort(tareas, new Comparator<Tarea>() {
            @Override
            public int compare(Tarea a, Tarea b) {
                return a.getFecha().compareTo(b.getFecha());
            }
        });

        // Limpia la lista previa y asigna las tareas ordenadas recien recopiladas
        listaTareasVencidas.clear();
        listaTareasVencidas.addAll(tareas);

        // Notifica al adaptador que el conjunto completo de datos sufrió cambios para redibujar
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Evalúa si la lista quedó vacía o con ítems para ajustar los componentes visuales
        actualizarEstadoLista();
    }

    // Instancia el adaptador pasando el listener que procesa la resolución/finalización de una tarea
    private void configurarAdaptador() {
        adapter = new TareaVencidaAdapter(listaTareasVencidas, new TareaVencidaAdapter.OnTareaCompletadaListener() {
            @Override
            public void onTareaCompletada(final Tarea tarea, int posicion) {
                // Envia una petición PATCH al backend actualizando el estado de la tarea a "FINALIZADO"
                ApiClient.getService().cambiarEstado(tarea.getId(), new EstadoRequest("FINALIZADO"))
                        .enqueue(new Callback<TareaResponse>() {
                            @Override
                            public void onResponse(Call<TareaResponse> call, Response<TareaResponse> response) {
                                // Si el servidor rechaza el cambio, notifica el error y revierte la vista
                                if (!response.isSuccessful()) {
                                    mensaje("No se pudo cambiar el estado (" + response.code() + ")");
                                    adapter.notifyDataSetChanged();
                                    return;
                                }

                                // Ubica el índice de la tarea en la lista local en memoria
                                int pos = listaTareasVencidas.indexOf(tarea);
                                if (pos == -1) return;
                                // Elimina la tarea del conjunto de datos en memoria
                                listaTareasVencidas.remove(pos);

                                // Notifica la eliminación de la posición específica al adaptador con animación suave
                                adapter.notifyItemRemoved(pos);
                                adapter.notifyItemRangeChanged(pos, listaTareasVencidas.size());

                                // Actualiza la visibilidad de vistas vacías y el indicador del total de tareas
                                actualizarEstadoLista();

                                // Muestra un Toast confirmando la resolución satisfactoria de la tarea
                                Toast.makeText(TareasVencidasActivity.this,
                                        "Tarea '" + tarea.getNombre() + "' resuelta correctamente",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<TareaResponse> call, Throwable t) {
                                mensaje("No se pudo conectar con el servidor");
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        });

        // Asigna el adaptador configurado al RecyclerView de tareas vencidas
        rvTareasVencidas.setAdapter(adapter);
    }

    // Alterna la visibilidad entre el aviso de 'Sin tareas vencidas' y el RecyclerView según la cantidad de elementos
    private void actualizarEstadoLista() {
        int cantidad = listaTareasVencidas.size();

        if (cantidad == 0) {
            // Si la cantidad es 0: oculta el RecyclerView y despliega la vista de sin tareas
            rvTareasVencidas.setVisibility(View.GONE);
            txtSinTareasVencidas.setVisibility(View.VISIBLE);
            txtTotalTareasVencidas.setText("Total vencidas: 0");
        } else {
            // Si hay tareas: muestra el RecyclerView, oculta la vista de vacio y actualiza el contador
            rvTareasVencidas.setVisibility(View.VISIBLE);
            txtSinTareasVencidas.setVisibility(View.GONE);
            txtTotalTareasVencidas.setText("Total vencidas: " + cantidad);
        }
    }

    // Método utilitario para emitir notificaciones emergentes cortas mediante Toast
    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}