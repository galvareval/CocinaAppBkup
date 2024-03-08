package vidal.juan.cocinapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class AdaptadorPedidosActivos extends BaseAdapter {

    private ArrayList<Pedido> pedidosActivos;
    private int R_layout_IdView;
    private Context contexto;

    public AdaptadorPedidosActivos(Context contexto, int R_Layout_IdView) {
        super();
        this.contexto = contexto;
        this.R_layout_IdView = R_Layout_IdView;
        this.pedidosActivos = new ArrayList<>();
    }
    public void actualizarLista(ArrayList<Pedido> nuevaLista) {
        pedidosActivos.clear();
        pedidosActivos.addAll(nuevaLista);
        notifyDataSetChanged();
    }
    public void setPedidosActivos(ArrayList<Pedido> pedidosActivos) {
        this.pedidosActivos = pedidosActivos;
        notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
    }

    public abstract void onEntrada(Pedido pedidoActivo, View view);

    @Override
    public int getCount() {
        return pedidosActivos.size();
    }

    @Override
    public Pedido getItem(int position) {
        return pedidosActivos.get(position);
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

        onEntrada(pedidosActivos.get(position), convertView);
        // Agregar registro al logcat para mostrar el pedido cada vez que se infle un elemento
        Pedido pedido = pedidosActivos.get(position);
        Log.d("PedidoInflado", "Pedido inflado: " + pedido.toString());
        return convertView;
    }
}
