package vidal.juan.cocinapp;

public class EncapsuladorEntradas {

    private String urlImagen;
    private String titulo;

    private String descripcion;
    private String precio;
    private int cantidadActual;

    private int cantidadMaxima;

    private int stock;


    public EncapsuladorEntradas(String urlImagen, String tituloEntrada, String descripcion, String precioEntrada, int cantidadMaxima, int stock) {
        this.urlImagen = urlImagen;
        this.titulo = tituloEntrada;
        this.descripcion = descripcion;
        this.precio = precioEntrada;
        this.cantidadMaxima = cantidadMaxima;
        this.stock = stock;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String get_textoTitulo() {
        return titulo;
    }

    public String get_Precio() {
        return precio;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(int cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public int getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(int cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
