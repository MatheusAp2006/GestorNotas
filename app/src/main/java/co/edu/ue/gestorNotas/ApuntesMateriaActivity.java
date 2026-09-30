package co.edu.ue.gestorNotas;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.ue.gestorNotas.Database.TareaBDHelper;

/**
 * Pantalla de Apuntes e Imágenes de UNA materia.
 * Permite dictar apuntes (micrófono), tomar fotos (cámara) y elegir imágenes (galería).
 * Todo se guarda en SQLite asociado al id de la materia.
 */
// Declaración de la clase pública ApuntesMateriaActivity que hereda de AppCompatActivity para gestionar la pantalla de apuntes e imágenes
public class ApuntesMateriaActivity extends AppCompatActivity {

    // Declaración de elementos gráficos (TextViews) para el título de la materia y mensajes de contenido vacío
    private TextView txtMateriaApuntes, txtSinApuntes, txtSinImagenes;
    // Declaración de contenedores lineales dinámicos donde se renderizan los textos de apuntes y las miniaturas de imágenes
    private LinearLayout contenedorApuntes, contenedorImagenes;
    // Declaración de botones para activar las funcionalidades de dictado por voz, foto de cámara, selección de galería y escritura manual
    private Button btnDictarApunte, btnTomarFoto, btnGaleria, btnEscribirApunte;

    // Instancia del helper de la base de datos local SQLite (TareaBDHelper)
    private TareaBDHelper bdHelper;
    // Variable para almacenar el nombre de la materia actual
    private String nombreMateria;
    // Variable para almacenar el ID único numérico de la materia actual
    private long materiaId;

    // Variable temporal para guardar la ruta absoluta de la foto tomada por la cámara mientras el proceso responde
    private String rutaFotoPendiente;

    // Lanzador de la API ActivityResultContracts para procesar el intento de reconocimiento de voz
    private ActivityResultLauncher<Intent> lanzadorVoz;
    // Lanzador de la API ActivityResultContracts para invocar la aplicación de cámara guardando el resultado en una URI
    private ActivityResultLauncher<Uri> lanzadorCamara;
    // Lanzador de la API ActivityResultContracts para seleccionar medios visuales (imágenes) desde el selector nativo
    private ActivityResultLauncher<PickVisualMediaRequest> lanzadorGaleria;

    // Lanzadores para solicitar de manera asíncrona permisos en tiempo de ejecución al sistema operativo
    private ActivityResultLauncher<String> permisoMicrofono;
    private ActivityResultLauncher<String> permisoCamara;
    private ActivityResultLauncher<String> permisoGaleria;

    // Sobrescribe el método del ciclo de vida onCreate que se ejecuta al instanciar la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ejecuta la lógica por defecto de la clase padre AppCompatActivity
        super.onCreate(savedInstanceState);
        // Vincula el diseño XML 'activity_apuntes_materia' con esta vista
        setContentView(R.layout.activity_apuntes_materia);

        // PASO 1: Enlaza las variables locales con los IDs correspondientes de los componentes en el XML
        txtMateriaApuntes = findViewById(R.id.txtMateriaApuntes);
        txtSinApuntes = findViewById(R.id.txtSinApuntes);
        txtSinImagenes = findViewById(R.id.txtSinImagenes);
        contenedorApuntes = findViewById(R.id.contenedorApuntes);
        contenedorImagenes = findViewById(R.id.contenedorImagenes);
        btnDictarApunte = findViewById(R.id.btnDictarApunte);
        btnEscribirApunte = findViewById(R.id.btnEscribirApunte);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGaleria = findViewById(R.id.btnGaleria);

        // PASO 2: Inicializa la base de datos local SQLite y recupera los extras pasados en el Intent desde InicioActivity
        bdHelper = new TareaBDHelper(this);
        nombreMateria = getIntent().getStringExtra("materia");
        materiaId = getIntent().getLongExtra("idMateria", -1);

        // Comprueba si no se recibió un ID válido de la materia; de ser así, informa y cierra la vista actual
        if (materiaId == -1) {
            Toast.makeText(this, "No se encontró la materia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Asigna el nombre de la materia al TextView principal del encabezado
        txtMateriaApuntes.setText(nombreMateria);

        // Si la pantalla fue destruida y recreada (por ejemplo, al rotar el teléfono), recupera la ruta de la foto en proceso
        if (savedInstanceState != null) {
            rutaFotoPendiente = savedInstanceState.getString("rutaFotoPendiente");
        }

        // PASO 3: Inicializa y registra todos los lanzadores de actividades y permisos
        configurarLanzadores();

        // PASO 4: Asigna el evento OnClick al botón de dictado de apunte evaluando previamente el permiso de micrófono
        btnDictarApunte.setOnClickListener(v -> {
            if (tienePermiso(Manifest.permission.RECORD_AUDIO)) {
                iniciarDictado();
            } else {
                permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        // Asigna el evento OnClick al botón de escribir apunte abriendo un diálogo manual con texto inicial vacío
        btnEscribirApunte.setOnClickListener(v -> mostrarDialogoGuardarApunte(""));

        // Asigna el evento OnClick al botón de tomar foto evaluando el permiso de la cámara
        btnTomarFoto.setOnClickListener(v -> {
            if (tienePermiso(Manifest.permission.CAMERA)) {
                tomarFoto();
            } else {
                permisoCamara.launch(Manifest.permission.CAMERA);
            }
        });

        // Asigna el evento OnClick al botón de galería evaluando el permiso de almacenamiento/imágenes acorde a la versión de Android
        btnGaleria.setOnClickListener(v -> {
            String permiso = permisoDeGaleria();
            if (tienePermiso(permiso)) {
                abrirGaleria();
            } else {
                permisoGaleria.launch(permiso);
            }
        });

        // PASO 5: Carga los apuntes y las imágenes existentes desde SQLite y los renderiza en pantalla
        cargarApuntes();
        cargarImagenes();
    }

    // Sobrescribe onSaveInstanceState para conservar el estado de la ruta de foto pendiente ante cambios de configuración
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("rutaFotoPendiente", rutaFotoPendiente);
    }

    // LANZADORES

    // Método privado para configurar la lógica de respuesta de los lanzadores asíncronos
    private void configurarLanzadores() {
        // Registra el lanzador para procesar el resultado de la interfaz de dictado de voz del sistema
        lanzadorVoz = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Verifica que la respuesta sea exitosa y contenga datos
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Extrae la lista de frases reconocidas por el motor de voz
                        ArrayList<String> textos = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        // Si se capturó algún texto, muestra el diálogo de confirmación/edición con la mejor coincidencia
                        if (textos != null && !textos.isEmpty()) {
                            mostrarDialogoGuardarApunte(textos.get(0));
                        }
                    }
                });

        // Registra el lanzador de captura de foto de la cámara
        lanzadorCamara = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                exito -> {
                    // Si existe una ruta pendiente de foto
                    if (rutaFotoPendiente != null) {
                        if (exito) {
                            // Guarda la ruta de la imagen en SQLite asociada al ID de la materia y recarga la interfaz
                            bdHelper.insertarImagen(materiaId, rutaFotoPendiente);
                            cargarImagenes();
                            Toast.makeText(this, "Foto guardada", Toast.LENGTH_SHORT).show();
                        } else {
                            // Elimina el archivo temporal creado si la captura fue cancelada
                            new File(rutaFotoPendiente).delete();
                        }
                        // Reinicia la variable de ruta pendiente
                        rutaFotoPendiente = null;
                    }
                });

        // Registra el lanzador del selector de medios de la galería
        lanzadorGaleria = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    // Si el usuario seleccionó una imagen (URI no nula), invoca el copiado del archivo a almacenamiento privado
                    if (uri != null) {
                        copiarImagenDeGaleria(uri);
                    }
                });

        // Registra el lanzador de petición del permiso de micrófono
        permisoMicrofono = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (concedido) iniciarDictado();
                    else avisarPermisoDenegado("micrófono");
                });

        // Registra el lanzador de petición del permiso de cámara
        permisoCamara = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (concedido) tomarFoto();
                    else avisarPermisoDenegado("cámara");
                });

        // Registra el lanzador de petición del permiso de galería
        permisoGaleria = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (concedido) abrirGaleria();
                    else avisarPermisoDenegado("galería");
                });

    }

    // PERMISOS

    // Comprueba de forma síncrona si la aplicación ya cuenta con un permiso específico concedido por el usuario
    private boolean tienePermiso(String permiso) {
        return ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED;
    }

    // Determina el nombre exacto del permiso de galería según la versión del sistema (Android 13+ o inferior)
    private String permisoDeGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        }
        return Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    // Muestra una notificación Toast avisando que un permiso fue denegado e indicando cómo activarlo en ajustes
    private void avisarPermisoDenegado(String recurso) {
        Toast.makeText(this,
                "Permiso de " + recurso + " denegado. Actívalo en Ajustes > Aplicaciones > gestorNotas > Permisos",
                Toast.LENGTH_LONG).show();
    }

    // Despliega el selector nativo de medios de Android filtrando únicamente para imágenes
    private void abrirGaleria() {
        lanzadorGaleria.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    // MICROFONO

    // Prepara e invoca el Intent para activar el reconocimiento de voz por micrófono
    private void iniciarDictado() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Define el modelo de lenguaje de forma libre
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Define el idioma basándose en la configuración regional por defecto del teléfono
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag());
        // Establece el mensaje que aparecerá en la pantalla del reconocedor de voz
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicta tu apunte...");
        try {
            lanzadorVoz.launch(intent);
        } catch (ActivityNotFoundException e) {
            // Maneja la excepción si el dispositivo no posee un motor de reconocimiento de voz instalado
            Toast.makeText(this, "Dictado no disponible en este dispositivo. Escribe tu apunte.",
                    Toast.LENGTH_LONG).show();
            mostrarDialogoGuardarApunte("");
        }
    }

    // Muestra un cuadro de diálogo con un campo editable de texto para escribir o revisar un apunte antes de guardarlo
    private void mostrarDialogoGuardarApunte(String textoInicial) {
        // Crea un campo de texto dinámico de múltiples líneas
        final EditText etApunte = new EditText(this);
        etApunte.setText(textoInicial);
        etApunte.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etApunte.setMinLines(3);
        etApunte.setGravity(Gravity.TOP);

        // Calcula un margen en píxeles acorde a la densidad de pantalla del dispositivo
        int margen = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout contenedor = new FrameLayout(this);
        contenedor.setPadding(margen, margen / 2, margen, 0);
        contenedor.addView(etApunte);

        // Construye y despliega la ventana emergente AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Guardar apunte en " + nombreMateria)
                .setView(contenedor)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // Extrae el texto del campo eliminando espacios blancos sobrantes
                    String texto = etApunte.getText().toString().trim();
                    if (texto.isEmpty()) {
                        Toast.makeText(this, "El apunte está vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Inserta el apunte en la base de datos SQLite asociada a esta materia
                    long id = bdHelper.insertarApunte(materiaId, texto);
                    if (id != -1) {
                        Toast.makeText(this, "Apunte guardado", Toast.LENGTH_SHORT).show();
                        // Actualiza la lista desplegada en pantalla
                        cargarApuntes();
                    } else {
                        Toast.makeText(this, "Error al guardar el apunte", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // GALERIA Y CAMARA

    // Crea un archivo JPG vacío con nombre único basado en marca de tiempo dentro del almacenamiento privado
    private File crearArchivoImagen(String prefijo) {
        File carpeta = new File(getFilesDir(), "imagenes");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return new File(carpeta, prefijo + System.currentTimeMillis() + ".jpg");
    }

    // Prepara la ruta de archivo y la URI mediante FileProvider para lanzar la app de cámara
    private void tomarFoto() {
        File archivo = crearArchivoImagen("foto_");
        rutaFotoPendiente = archivo.getAbsolutePath();
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
        try {
            lanzadorCamara.launch(uri);
        } catch (ActivityNotFoundException e) {
            rutaFotoPendiente = null;
            Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    // Copia un archivo de imagen seleccionado desde la galería externa hacia el directorio interno privado de la app
    private void copiarImagenDeGaleria(Uri uri) {
        File destino = crearArchivoImagen("galeria_");
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(destino)) {
            if (in == null) {
                throw new IOException("No se pudo abrir la imagen");
            }
            byte[] buffer = new byte[8192];
            int leidos;
            // Lee el flujo de datos por bloques de 8KB y los escribe en el archivo interno de destino
            while ((leidos = in.read(buffer)) > 0) {
                out.write(buffer, 0, leidos);
            }
            // Almacena la ruta local resultante en la base de datos
            bdHelper.insertarImagen(materiaId, destino.getAbsolutePath());
            cargarImagenes();
            Toast.makeText(this, "Imagen agregada", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al copiar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // MOSTRAR EL CONTENIDO

    // Consulta los apuntes guardados de la materia en SQLite y los agrega dinámicamente al contenedor
    private void cargarApuntes() {
        contenedorApuntes.removeAllViews();
        List<String> apuntes = bdHelper.obtenerApuntes(materiaId);
        // Muestra u oculta el mensaje de "Sin apuntes" según si la lista está vacía
        txtSinApuntes.setVisibility(apuntes.isEmpty() ? View.VISIBLE : View.GONE);

        // Itera sobre los textos obtenidos y genera un TextView por cada apunte
        for (String apunte : apuntes) {
            TextView tv = new TextView(this);
            tv.setText("• " + apunte);
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 8);
            contenedorApuntes.addView(tv);
        }
    }

    // Consulta las rutas de imágenes de la materia en SQLite y renderiza miniaturas dinámicas
    private void cargarImagenes() {
        contenedorImagenes.removeAllViews();
        List<String> rutas = bdHelper.obtenerImagenes(materiaId);
        // Muestra u oculta el mensaje de "Sin imágenes" según si la lista está vacía
        txtSinImagenes.setVisibility(rutas.isEmpty() ? View.VISIBLE : View.GONE);

        // Calcula el tamaño en píxeles equivalente a 120dp para la miniatura
        int tamano = (int) (120 * getResources().getDisplayMetrics().density);
        for (String ruta : rutas) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tamano, tamano);
            params.setMargins(0, 0, 16, 0);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // Carga y asigna el Bitmap reducido corregido en orientación
            iv.setImageBitmap(cargarMiniatura(ruta, tamano));
            contenedorImagenes.addView(iv);
        }
    }

    // Carga un Bitmap diezmado/escalado para reducir uso de memoria RAM y corrige la rotación EXIF de la cámara
    private Bitmap cargarMiniatura(String ruta, int tamano) {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        // inJustDecodeBounds = true permite leer los metadatos de dimensiones de la imagen sin cargar el Bitmap a RAM
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ruta, opciones);

        // Calcula el factor de submuestreo/escala (potencia de 2) para aproximar la imagen al tamaño deseado
        int escala = 1;
        while (opciones.outWidth / (escala * 2) >= tamano
                && opciones.outHeight / (escala * 2) >= tamano) {
            escala *= 2;
        }
        opciones.inSampleSize = escala;
        opciones.inJustDecodeBounds = false;
        // Decodifica la imagen aplicando el factor de escala calculado
        Bitmap bitmap = BitmapFactory.decodeFile(ruta, opciones);
        if (bitmap == null) return null;

        // Lee los metadatos EXIF de la imagen para detectar si viene con rotación de captura
        int grados = 0;
        try {
            ExifInterface exif = new ExifInterface(ruta);
            int orientacion = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientacion == ExifInterface.ORIENTATION_ROTATE_90) grados = 90;
            else if (orientacion == ExifInterface.ORIENTATION_ROTATE_180) grados = 180;
            else if (orientacion == ExifInterface.ORIENTATION_ROTATE_270) grados = 270;
        } catch (IOException e) {
            // Si la imagen no posee metadatos EXIF válidos, se ignora la rotación
        }
        // Aplica la rotación sobre el Bitmap usando una matriz si se detectó algún ángulo distinto de 0
        if (grados != 0) {
            Matrix matriz = new Matrix();
            matriz.postRotate(grados);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matriz, true);
        }
        return bitmap;
    }
}