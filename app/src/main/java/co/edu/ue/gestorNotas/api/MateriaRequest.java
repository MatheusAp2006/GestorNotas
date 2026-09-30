package co.edu.ue.gestorNotas.api;

public class MateriaRequest {
    private String nombreMateria;
    private String profesor;
    private String colorHex;

    public MateriaRequest(String nombreMateria, String profesor, String colorHex) {
        this.nombreMateria = nombreMateria;
        this.profesor = profesor;
        this.colorHex = colorHex;
    }
}