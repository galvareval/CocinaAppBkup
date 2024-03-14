package vidal.juan.cocinapp;

/**
 * Clase de detalles de pedidos no parcelable, ya que firebase ignora la implemetacion en detalles de pediddo y pasa el campo stability a la BDD
 * Hay que recrear el objeto detalles sin implemetar parcel
 */
public class DetallePedidoNoParcel {
    private String racion;
    private int cantidad;
    private double precio;
    private String precioRacion;
    private String pedidoMaxRacion ;
    private String stockRacion;
    private String stockOriginal;

    public DetallePedidoNoParcel(String racion, int cantidad, double precio, String precioRacion, String pedidoMaxRacion, String stockRacion, String stockOriginal) {
        this.racion = racion;
        this.cantidad = cantidad;
        this.precio = precio;
        this.precioRacion = precioRacion;
        this.pedidoMaxRacion = pedidoMaxRacion;
        this.stockRacion = stockRacion;
        this.stockOriginal = stockOriginal;
    }

    public DetallePedidoNoParcel(String racion, int cantidad, double precio) {
        this.racion = racion;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    // Constructor para firebase
    public DetallePedidoNoParcel() {
    }


    // GET/SET

    public String getStockOriginal() {
        return stockOriginal;
    }

    public void setStockOriginal(String stockOriginal) {
        this.stockOriginal = stockOriginal;
    }

    public String getPrecioRacion() {
        return precioRacion;
    }

    public void setPrecioRacion(String precioRacion) {
        this.precioRacion = precioRacion;
    }

    public String getPedidoMaxRacion() {
        return pedidoMaxRacion;
    }

    public void setPedidoMaxRacion(String pedidoMaxRacion) {
        this.pedidoMaxRacion = pedidoMaxRacion;
    }

    public String getStockRacion() {
        return stockRacion;
    }

    public void setStockRacion(String stockRacion) {
        this.stockRacion = stockRacion;
    }


    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getRacion() {
        return racion;
    }

    public void setRacion(String racion) {
        this.racion = racion;
    }

    // ToString
    @Override
    public String toString() {
        return "DetallePedidoNoparcel{" +
                "cantidad=" + cantidad +
                ", precio=" + precio +
                ", racion='" + racion + '\'' +
                ", precioRacion='" + precioRacion + '\'' +
                ", pedidoMaxRacion=" + pedidoMaxRacion +
                ", stockRacion='" + stockRacion + '\'' +
                ", stockOriginal='" + stockOriginal + '\'' +
                '}';
    }
}
