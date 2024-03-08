package vidal.juan.cocinapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class AdaptadorDetallesNoparcel extends BaseAdapter {

    private List<DetallePedidoNoParcel> detallesPedidoNoParcel;
    private int R_layout_IdView;
    private Context contexto;

    public AdaptadorDetallesNoparcel(Context contexto, int R_Layout_IdView, List<DetallePedidoNoParcel> detallesPedidoNoParcel) {
        super();
        this.contexto = contexto;
        this.detallesPedidoNoParcel = new ArrayList<>(); // Crear una nueva lista vacía
        this.R_layout_IdView = R_Layout_IdView;
        if (detallesPedidoNoParcel != null) {
            this.detallesPedidoNoParcel.addAll(detallesPedidoNoParcel); // Agregar los detalles existentes a la lista
        }
    }


    public abstract void onEntrada(DetallePedidoNoParcel detallePedidoNoParcel, View view);

    @Override
    public int getCount() {
        return detallesPedidoNoParcel.size();
    }

    @Override
    public DetallePedidoNoParcel getItem(int position) {
        return detallesPedidoNoParcel.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)
                    contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R_layout_IdView, null);

        }

        onEntrada(detallesPedidoNoParcel.get(position), convertView);
        Log.d("Onentrada", "Onentrada ejecutado:  Psition: " + (position) + " Count " + getCount() );
        return convertView;
    }
}
