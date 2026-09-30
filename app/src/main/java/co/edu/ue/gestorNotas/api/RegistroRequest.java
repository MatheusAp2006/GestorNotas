package co.edu.ue.gestorNotas.api;

public class RegistroRequest {
    private String nombre;
    private String email;
    private String password;

    public RegistroRequest(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }
}