package co.edu.ue.gestorNotas.api;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("tasky_session", Context.MODE_PRIVATE);
    }

    public void guardar(String token, String nombre, String email) {
        prefs.edit()
                .putString("token", token)
                .putString("nombre", nombre)
                .putString("email", email)
                .apply();
    }

    public String getToken() { return prefs.getString("token", null); }
    public String getNombre() { return prefs.getString("nombre", null); }

    public void cerrarSesion() { prefs.edit().clear().apply(); }
}