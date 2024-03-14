package vidal.juan.cocinapp;

import android.os.Parcel;
import android.os.Parcelable;

public class DetallePedido implements Parcelable {
    private String racion;
    private int cantidad;
    private double precio;
    private String precioRacion;
    private String pedidoMaxRacion;
    private String stockRacion;
    private String stockOriginal;

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

    public String getStockOriginal() {
        return stockOriginal;
    }

    public void setStockOriginal(String stockOriginal) {
        this.stockOriginal = stockOriginal;
    }

    // Constructor necesario para Parcelable
    private DetallePedido(Parcel in) {
        racion = in.readString();
        cantidad = in.readInt();
        precio = in.readDouble();
        precioRacion = in.readString();
        pedidoMaxRacion = in.readString();
        stockRacion = in.readString();
        stockOriginal = in.readString();
    }

    // Constructor para firebase
    public DetallePedido() {
    }

    public DetallePedido(String racion, int cantidad, double precio, String precioRacion, String pedidoMaxRacion, String stockRacion, String stockOriginal) {
        this.racion = racion;
        this.cantidad = cantidad;
        this.precio = precio;
        this.precioRacion = precioRacion;
        this.pedidoMaxRacion = pedidoMaxRacion;
        this.stockRacion = stockRacion;
        this.stockOriginal = stockOriginal;
    }

    public DetallePedido(String racion, int cantidad, double precio) {
        this.racion = racion;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    // GET/SET
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
        return "DetallePedido{" +
                "racion='" + racion + '\'' +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", precioRacion='" + precioRacion + '\'' +
                ", pedidoMaxRacion='" + pedidoMaxRacion + '\'' +
                ", stockRacion='" + stockRacion + '\'' +
                ", stockOriginal='" + stockOriginal + '\'' +
                '}';
    }

    // Métodos Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(racion);
        dest.writeInt(cantidad);
        dest.writeDouble(precio);
        dest.writeString(precioRacion);
        dest.writeString(pedidoMaxRacion);
        dest.writeString(stockRacion);
        dest.writeString(stockOriginal);
    }

    public static final Parcelable.Creator<DetallePedido> CREATOR = new Parcelable.Creator<DetallePedido>() {
        @Override
        public DetallePedido createFromParcel(Parcel in) {
            return new DetallePedido(in);
        }

        @Override
        public DetallePedido[] newArray(int size) {
            return new DetallePedido[size];
        }
    };
}
