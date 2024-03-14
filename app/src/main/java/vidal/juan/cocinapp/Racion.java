package vidal.juan.cocinapp;

import java.util.List;

/**
 * Clase que representa una racion de la BBDD
 */
public class Racion {

    private List<String> alergenos;
    private String descripcion;
    private String foto;
    private int pedido_max;
    private String precio;
    private String stock;
    //Para setear la url
    private final String URL_FOTOS = "https://firebasestorage.googleapis.com/v0/b/cocinaapp-7da53.appspot.com/o/";
    private final String URL_SUFIJO = "?alt=media";
    //La url de la imagen siempre tiene esta estructura, no es necesario setearlo por lo que no hace fatla set
    public String getUrlImagen() {
        urlImagen = URL_FOTOS + foto + URL_SUFIJO;
        return urlImagen;
    }

    private String urlImagen;
    public Racion() {
    }

    public Racion(List<String> alergenos, String descripcion, String foto, int pedido_max, String precio, String stock) {
        this.alergenos = alergenos;
        this.descripcion = descripcion;
        this.foto = foto;
        this.pedido_max = pedido_max;
        this.precio = precio;
        this.stock = stock;
    }

    public List<String> getAlergenos() {
        return alergenos;
    }

    public void setAlergenos(List<String> alergenos) {
        this.alergenos = alergenos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public int getPedido_max() {
        return pedido_max;
    }

    public void setPedido_max(int pedido_max) {
        this.pedido_max = pedido_max;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }
    @Override
    public String toString() {
        return "Racion{" +
                "alergenos=" + alergenos +
                ", descripcion='" + descripcion + '\'' +
                ", foto='" + foto + '\'' +
                ", pedido_max=" + pedido_max +
                ", precio='" + precio + '\'' +
                ", stock='" + stock + '\'' +
                '}';
    }

}
