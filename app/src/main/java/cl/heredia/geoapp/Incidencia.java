package cl.heredia.geoapp;

/**
 * Clase Modelo (POJO) que representa una Incidencia reportada en terreno.
 * Se utiliza tanto para enviar datos a Firebase Firestore como para deserializarlos.
 */
public class Incidencia {

    private String id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private String fechaHora;
    private double latitud;
    private double longitud;

    /**
     * Constructor vacío OBLIGATORIO para Firebase Cloud Firestore.
     */
    public Incidencia() {
    }

    /**
     * Constructor con parámetros para crear una nueva incidencia desde el formulario.
     */
    public Incidencia(String id, String titulo, String descripcion, String categoria,
                String fechaHora, double latitud, double longitud) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaHora = fechaHora;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Métodos Getter y Setter (necesarios para que Firestore lea y escriba cada propiedad)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
