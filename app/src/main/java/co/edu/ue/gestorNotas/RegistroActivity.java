package co.edu.ue.gestorNotas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.AuthResponse;
import co.edu.ue.gestorNotas.api.RegistroRequest;
import co.edu.ue.gestorNotas.api.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Declaración de la clase pública RegistroActivity que extiende AppCompatActivity para gestionar la vista de registro de usuarios
public class RegistroActivity extends AppCompatActivity {

    // Declaración de los campos de entrada de texto para nombre completo, correo, contraseña y confirmación
    private EditText etNombreCompleto;
    private EditText etCorreoRegistro;
    private EditText etPasswordRegistro;
    private EditText etConfirmarPassword;
    // Declaración del botón para enviar el formulario de registro
    private Button btnRegistrarse;
    // Declaración del TextView para navegar de regreso al inicio de sesión
    private TextView tvYaTengoCuenta;

    // Sobrescribe el método del ciclo de vida onCreate que se ejecuta al instanciar la pantalla de registro
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ejecuta la lógica base de la clase padre AppCompatActivity
        super.onCreate(savedInstanceState);
        // Vincula el diseño XML 'activity_registro' con la vista de registro
        setContentView(R.layout.activity_registro);

        // Enlaza las variables locales con los componentes definidos en el XML usando sus IDs
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etPasswordRegistro = findViewById(R.id.etPasswordRegistro);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        tvYaTengoCuenta = findViewById(R.id.tvYaTengoCuenta);

        // Asigna el evento OnClick al botón de registro para invocar las validaciones
        btnRegistrarse.setOnClickListener(v -> validarYRegistrar());

        // Asigna el evento OnClick al texto "Ya tengo cuenta" para cerrar la pantalla y volver al login
        tvYaTengoCuenta.setOnClickListener(v -> finish());
    }

    // Método privado que realiza las validaciones de campos del formulario antes de proceder con el registro
    private void validarYRegistrar() {
        // Extrae el contenido de los campos de texto eliminando espacios en blanco en los extremos
        String nombre = etNombreCompleto.getText().toString().trim();
        String correo = etCorreoRegistro.getText().toString().trim();
        String password = etPasswordRegistro.getText().toString().trim();
        String confirmarPassword = etConfirmarPassword.getText().toString().trim();

        // Valida que el campo de nombre no esté vacío
        if (nombre.isEmpty()) {
            etNombreCompleto.setError("Campo requerido");
            etNombreCompleto.requestFocus();
            return;
        }

        // Valida que el campo de correo electrónico no esté vacío
        if (correo.isEmpty()) {
            etCorreoRegistro.setError("Campo requerido");
            etCorreoRegistro.requestFocus();
            return;
        }

        // Valida que el campo de contraseña no esté vacío
        if (password.isEmpty()) {
            etPasswordRegistro.setError("Campo requerido");
            etPasswordRegistro.requestFocus();
            return;
        }

        // Valida que el campo de confirmación de contraseña no esté vacío
        if (confirmarPassword.isEmpty()) {
            etConfirmarPassword.setError("Campo requerido");
            etConfirmarPassword.requestFocus();
            return;
        }

        // Valida que la estructura del correo electrónico cumpla con el patrón estándar de un email válido
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoRegistro.setError("Ingrese un correo electrónico válido");
            etCorreoRegistro.requestFocus();
            return;
        }

        // Valida que la contraseña tenga una longitud mínima de 6 caracteres
        if (password.length() < 6) {
            etPasswordRegistro.setError("La contraseña debe tener al menos 6 caracteres");
            etPasswordRegistro.requestFocus();
            return;
        }

        // Valida que la contraseña y su confirmación coincidan exactamente
        if (!password.equals(confirmarPassword)) {
            etConfirmarPassword.setError("Las contraseñas no coinciden");
            etConfirmarPassword.requestFocus();
            return;
        }

        // Si todas las validaciones son exitosas, invoca el método para realizar la petición HTTP al servidor
        registrar(nombre, correo, password);
    }

    // Método privado que ejecuta la llamada asíncrona a la API REST mediante Retrofit
    private void registrar(String nombre, String correo, String password) {
        // Deshabilita temporalmente el botón para prevenir múltiples envíos accidentales
        btnRegistrarse.setEnabled(false);

        // Envía la solicitud de registro al endpoint definido en ApiService
        ApiClient.getService().registro(new RegistroRequest(nombre, correo, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        // Vuelve a habilitar el botón tras recibir la respuesta del servidor
                        btnRegistrarse.setEnabled(true);
                        // Verifica si la respuesta fue exitosa (código HTTP 2xx) y contiene el objeto AuthResponse
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            // Guarda los datos de autenticación (token, nombre y correo) en SharedPreferences
                            SessionManager session = new SessionManager(RegistroActivity.this);
                            session.guardar(auth.getToken(), auth.getNombre(), auth.getEmail());
                            // Asigna el token recién generado al cliente global de ApiClient
                            ApiClient.setToken(auth.getToken());

                            // Muestra mensaje de éxito, redirige a la pantalla principal y destruye la pila de actividades anteriores
                            Toast.makeText(RegistroActivity.this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistroActivity.this, InicioActivity.class));
                            finishAffinity();
                        } else if (response.code() == 409) {
                            // Muestra alerta específica si el correo ingresado ya existe en la base de datos (conflicto)
                            Toast.makeText(RegistroActivity.this,
                                    "Ese correo ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            // Muestra una notificación con el código de error HTTP en caso de cualquier otra respuesta del servidor
                            Toast.makeText(RegistroActivity.this,
                                    "Error al registrar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        // Reactiva el botón e informa al usuario en caso de fallos de red o falta de conexión
                        btnRegistrarse.setEnabled(true);
                        Toast.makeText(RegistroActivity.this,
                                "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}