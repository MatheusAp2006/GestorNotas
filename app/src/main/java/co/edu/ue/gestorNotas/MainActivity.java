package co.edu.ue.gestorNotas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.AuthResponse;
import co.edu.ue.gestorNotas.api.LoginRequest;
import co.edu.ue.gestorNotas.api.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Declaración de la clase pública MainActivity que hereda de AppCompatActivity para gestionar la pantalla de inicio de sesión
public class MainActivity extends AppCompatActivity {

    // Declaración de los campos de texto para la entrada de correo electrónico y contraseña
    private EditText etCorreo;
    private EditText etPassword;
    // Declaración del botón para enviar el formulario de autenticación
    private Button btnIniciarSesion;
    // Gestor de sesión preferencial para leer y guardar la persistencia del token del usuario
    private SessionManager session;

    // Sobrescribe el método del ciclo de vida onCreate que se ejecuta al instanciar la pantalla de login
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ejecuta la inicialización estándar de la clase padre AppCompatActivity
        super.onCreate(savedInstanceState);

        // Inicializa el administrador de sesión con el contexto de esta actividad
        session = new SessionManager(this);
        // Si el usuario ya tiene un token guardado (sesión activa), se salta la pantalla de login
        if (session.getToken() != null) {
            // Asigna el token existente a la instancia global de ApiClient para futuras peticiones HTTP
            ApiClient.setToken(session.getToken());
            // Redirige directamente a la pantalla principal (InicioActivity)
            irAInicio();
            return;
        }

        // Vincula el diseño XML 'activity_main' con la vista de inicio de sesión
        setContentView(R.layout.activity_main);

        // Enlaza las variables locales con sus respectivos elementos de la interfaz por medio de su ID
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        // Asigna el evento OnClick al botón de iniciar sesión invocando el método de autenticación
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Vincula el TextView para la creación de cuentas e inicia la actividad de RegistroActivity al hacer clic
        TextView tvCrearCuenta = findViewById(R.id.tvCrearCuenta);
        tvCrearCuenta.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegistroActivity.class)));
    }

    // Método privado que valida las entradas del usuario y envía la solicitud HTTP de login
    private void iniciarSesion() {
        // Extrae las cadenas de texto ingresadas en correo y contraseña eliminando espacios sobrantes
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Valida que ninguno de los campos requeridos esté vacío
        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilita temporalmente el botón para evitar múltiples clics continuos mientras se procesa la petición
        btnIniciarSesion.setEnabled(false);

        // Realiza la llamada asíncrona hacia el endpoint de autenticación mediante Retrofit
        ApiClient.getService().login(new LoginRequest(correo, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        // Vuelve a habilitar el botón una vez obtenida la respuesta del servidor
                        btnIniciarSesion.setEnabled(true);
                        // Comprueba si la respuesta HTTP fue exitosa (código 2xx) y contiene un cuerpo con datos
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            // Guarda el token JWT, nombre y correo en SharedPreferences
                            session.guardar(auth.getToken(), auth.getNombre(), auth.getEmail());
                            // Asigna el token al ApiClient estático para autorizar las peticiones posteriores
                            ApiClient.setToken(auth.getToken());
                            // Redirige al usuario a la vista principal
                            irAInicio();
                        } else if (response.code() == 401) {
                            // Muestra una alerta si las credenciales ingresadas son incorrectas
                            Toast.makeText(MainActivity.this,
                                    "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        } else {
                            // Muestra una notificación genérica con el código de error HTTP devuelto por la API
                            Toast.makeText(MainActivity.this,
                                    "Error del servidor (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        // Vuelve a habilitar el botón y notifica en caso de un error de red o falta de conexión
                        btnIniciarSesion.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método privado para realizar la transición de pantalla hacia InicioActivity y cerrar MainActivity
    private void irAInicio() {
        startActivity(new Intent(MainActivity.this, InicioActivity.class));
        finish();
    }
}