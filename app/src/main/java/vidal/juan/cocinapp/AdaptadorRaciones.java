package vidal.juan.cocinapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class AdaptadorRaciones extends BaseAdapter {

    private ArrayList<Racion> raciones;
    private int R_layout_IdView;
    protected Context contexto;

    public AdaptadorRaciones(Context contexto, int R_Layout_IdView, ArrayList<Racion> raciones) {
        super();
        this.contexto = contexto;
        this.raciones = raciones;
        this.R_layout_IdView = R_Layout_IdView;
    }

    public abstract void onEntrada(Racion racion, View view);

    @Override
    public int getCount() {
        return raciones.size();
    }

    @Override
    public Racion getItem(int position) {
        return raciones.get(position);
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

        onEntrada(raciones.get(position), convertView);

        return convertView;
    }
}
