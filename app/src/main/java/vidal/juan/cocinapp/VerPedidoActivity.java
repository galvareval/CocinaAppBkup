package vidal.juan.cocinapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class VerPedidoActivity extends AppCompatActivity {

    private Button cancelVerPedidosActivosButton;
    private ListView listaPedidosActivos;
    private FirebaseUser usuarioLogeado;
    private AdaptadorPedidosActivos adaptadorPedidosActivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pedido);
        Log.d("ActivityLifecycle", "onCreate() VerPedidos");
        cancelVerPedidosActivosButton = findViewById(R.id.cancelVerPedidosActivosButton);
        listaPedidosActivos = findViewById(R.id.listaPedidosActivos);

        usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();

        cancelVerPedidosActivosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volverPprincipal();
            }
        });

        adaptadorPedidosActivos = new AdaptadorPedidosActivos(VerPedidoActivity.this, R.layout.pedidos_activos_vista) {
            @Override
            public void onEntrada(Pedido pedidoActivo, View view) {
                // Referencias a los elementos de la vista
                TextView textViewFechaPedido = view.findViewById(R.id.textViewFechaPedido);
                TextView textViewFechaEntrega = view.findViewById(R.id.textViewFechaEntrega);
                TextView textViewEstado = view.findViewById(R.id.textViewEstado);
                TextView textViewPrecio = view.findViewById(R.id.textViewPrecio);
                TextView textViewComentarios = view.findViewById(R.id.textViewComentarios);
                TextView textViewIdPedido = view.findViewById(R.id.textViewIdPedido);
                TableRow filaPedidoColor = view.findViewById(R.id.filaPedidoColor);
                LinearLayout linarLayoutDetallePedido = view.findViewById(R.id.linarLayoutDetallePedido);

                // Cargar los datos en los campos
                textViewIdPedido.setText(getString(R.string.idPedidoString) + String.valueOf(pedidoActivo.getIdPedido()).substring(3, 7));
                textViewFechaPedido.setText(String.valueOf(pedidoActivo.getFecha_pedido()));
                textViewFechaEntrega.setText(String.valueOf(pedidoActivo.getFecha_entrega()));
                textViewEstado.setText(String.valueOf(pedidoActivo.getEstado()));
                Locale locale = Locale.US;//Para poner el . como serparador
                textViewPrecio.setText(String.format(locale,"%.2f",pedidoActivo.getPrecio_total()) + "\u20AC");
                //textViewPrecio.setText(String.valueOf(pedidoActivo.getPrecio_total()) + "\u20AC"); TODO quitar esto
                textViewComentarios.setText(getString(R.string.comentarios) +": " +  String.valueOf(pedidoActivo.getComentarios()));

                // Evento de click en el pedido para pasar a los detalles del pedido
                linarLayoutDetallePedido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        verDetallesDEPedido(pedidoActivo.getIdPedido());
                    }
                });

                // Cambiar color en función del estado
                if (pedidoActivo.getEstado().equals("preparar")) {
                    int drawableResourceId = getResources().getIdentifier("preparar_borde", "drawable", getPackageName());
                    //filaPedidoColor.setBackgroundColor(getResources().getColor(R.color.prepararPedidoColor));
                    //textViewComentarios.setBackgroundColor(getResources().getColor(R.color.prepararPedidoColor));
                    textViewIdPedido.setBackgroundResource(drawableResourceId);
                } else if (pedidoActivo.getEstado().equals("recoger")) {
                    //filaPedidoColor.setBackgroundColor(getResources().getColor(R.color.recogerPedidoColor));
                    //textViewComentarios.setBackgroundColor(getResources().getColor(R.color.recogerPedidoColor));
                    int drawableResourceId = getResources().getIdentifier("recoger_borde", "drawable", getPackageName());
                    textViewIdPedido.setBackgroundResource(drawableResourceId);
                } else {
                    filaPedidoColor.setBackgroundColor(getResources().getColor(R.color.defPedidoColor));
                    textViewComentarios.setBackgroundColor(getResources().getColor(R.color.defPedidoColor));
                }
            }
        };

        listaPedidosActivos.setAdapter(adaptadorPedidosActivos);

        obtenerPedidos();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLifecycle", "onDestroy() Verpedido");
    }

    private void obtenerPedidos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Log.d("database", "DatabaseReference" + databaseReference);

        databaseReference.child("pedidos").orderByChild("usuario").equalTo(usuarioLogeado.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Pedido> pedidosActivos = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Pedido pedido = snapshot.getValue(Pedido.class);
                            pedido.setIdPedido(snapshot.getKey());
                            if (pedido != null && ("preparar".equals(pedido.getEstado()) || "recoger".equals(pedido.getEstado()))) {
                                if (pedido.getEstado().equals("recoger")) {
                                    pedidosActivos.add(0, pedido);
                                } else {
                                    pedidosActivos.add(pedido);
                                }
                            }
                        }
                        // Actualizar la lista de pedidos en el adaptador
                        Log.d("VerPedidoActivity", "Atulizando lista");
                        adaptadorPedidosActivos.actualizarLista(pedidosActivos);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Error obtener pedidos", "Error al obtener los pedidos:", databaseError.toException());
                    }
                });
    }

    private void verDetallesDEPedido(String idPedido) {
        //Consultar raciones ;Accer al nodo racion para que funcio el cambio de la racion; si no se hace esta conxión con el listener luego no funciona la modificación
        //No se porque pero es la unica solución que he encontrado de momento 28/02/2023
        DatabaseReference databaseReferenceRacion = FirebaseDatabase.getInstance().getReference().child("raciones");
        databaseReferenceRacion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Racion racion = snapshot.getValue(Racion.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Intent verDetallesPedido = new Intent(VerPedidoActivity.this, VerDetallesPedidoActivity.class);
        verDetallesPedido.putExtra("idPedido", idPedido);
        startActivity(verDetallesPedido);
        finish();
    }

    /**
     * Volver Pantalla principal
     */
    private void volverPprincipal() {
        /*Intent intent = new Intent(VerPedidoActivity.this, PantallaPrincipalActivity.class);
        startActivity(intent);*/
        finish();
    }

}
