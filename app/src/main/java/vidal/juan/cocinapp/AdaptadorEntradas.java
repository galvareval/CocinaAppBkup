package vidal.juan.cocinapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class AdaptadorEntradas extends BaseAdapter {

    private ArrayList<EncapsuladorEntradas> entradas;
    private int R_layout_IdView;
    protected Context contexto;

    public AdaptadorEntradas(Context contexto, int R_Layout_IdView, ArrayList<EncapsuladorEntradas> entradas) {
        super();
        this.contexto = contexto;
        this.entradas = entradas;
        this.R_layout_IdView = R_Layout_IdView;
    }

    public abstract void onEntrada(EncapsuladorEntradas entrada, View view);

    @Override
    public int getCount() {
        return entradas.size();
    }

    @Override
    public EncapsuladorEntradas getItem(int position) {
        return entradas.get(position);
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

        onEntrada(entradas.get(position), convertView);

        return convertView;
    }
}
