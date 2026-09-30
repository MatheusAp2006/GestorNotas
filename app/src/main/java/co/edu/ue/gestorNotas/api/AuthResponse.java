package co.edu.ue.gestorNotas.api;

public class AuthResponse {
    private String token;
    private long idUsuario;
    private String nombre;
    private String email;

    public String getToken() { return token; }
    public long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}