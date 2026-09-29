package co.edu.ue.gestorNotas.api;

public class MateriaResponse {
    private long id;
    private String nombreMateria;
    private String profesor;
    private String colorHex;

    public long getId() { return id; }
    public String getNombreMateria() { return nombreMateria; }
    public String getProfesor() { return profesor; }
    public String getColorHex() { return colorHex; }
}