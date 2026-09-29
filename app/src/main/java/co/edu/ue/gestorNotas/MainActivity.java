package co.edu.ue.gestorNotas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.gestorNotas.api.ApiClient;
import co.edu.ue.gestorNotas.api.AuthResponse;
import co.edu.ue.gestorNotas.api.LoginRequest;
import co.edu.ue.gestorNotas.api.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etPassword;
    private Button btnIniciarSesion;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay una sesión guardada, se salta el login
        session = new SessionManager(this);
        if (session.getToken() != null) {
            ApiClient.setToken(session.getToken());
            irAInicio();
            return;
        }

        setContentView(R.layout.activity_main);

        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        btnIniciarSesion.setEnabled(false);

        ApiClient.getService().login(new LoginRequest(correo, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnIniciarSesion.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            session.guardar(auth.getToken(), auth.getNombre(), auth.getEmail());
                            ApiClient.setToken(auth.getToken());
                            irAInicio();
                        } else if (response.code() == 401) {
                            Toast.makeText(MainActivity.this,
                                    "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Error del servidor (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnIniciarSesion.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irAInicio() {
        startActivity(new Intent(MainActivity.this, InicioActivity.class));
        finish();
    }
}