package vidal.juan.cocinapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class VerDetallesPedidoActivity extends AppCompatActivity {

    private Button volverPedidosButton,modPediddoButton,eliminarPedidoButton;
    private ListView listaDetalle;
    private TextView fechaPedidoDetalleText,fechaEntregaDetalleText,cometariosDetalleText,totalDetalleText,idPedidoTextView,textModPedidoInfo;
    private String idPedido;
    private LinearLayout layoutEditarPedido;
    //Para la url de la imagen
    private final String URL_FOTOS = "https://firebasestorage.googleapis.com/v0/b/cocinaapp-7da53.appspot.com/o/";
    private final String URL_SUFIJO = "?alt=media";

    @Override
    public void onBackPressed() {

        volverPedidos();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ActivityLifecycle", "onCreate() VerDetalles");
        setContentView(R.layout.activity_ver_detalles_pedido);
        //referencia a items xml
        volverPedidosButton = findViewById(R.id.volverPedidosButton);
        modPediddoButton = findViewById(R.id.modPediddoButton);
        eliminarPedidoButton = findViewById(R.id.eliminarPedidoButton);
        listaDetalle = findViewById(R.id.listaDetalle);
        fechaPedidoDetalleText = findViewById(R.id.fechaPedidoDetalleText);
        fechaEntregaDetalleText = findViewById(R.id.fechaEntregaDetalleText);
        cometariosDetalleText = findViewById(R.id.cometariosDetalleText);
        totalDetalleText = findViewById(R.id.totalDetalleText);
        idPedidoTextView  = findViewById(R.id.idPedidoTextView);
        textModPedidoInfo  = findViewById(R.id.textModPedidoInfo);
        layoutEditarPedido = findViewById(R.id.layoutModPedido);
        //Funcionalidad botones
        //Volver a la pantalla principal
        volverPedidosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volverPedidos();
            }
        });

        //Modificar pedido
        modPediddoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modificarPedido();
            }
        });


        // Recuperar detalles seleccionados y precio total de la anterior actividad
        idPedido = getIntent().getStringExtra("idPedido");
        Log.d("PedidoRecib", "Pedido: " + idPedido);
        //buscar y mostrar los detalles del pedido a partir de id

        buscarPedido(idPedido);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLifecycle", "onDestroy() VerDetalles");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ActivityLifecycle", "onPause() Verdetalles");

    }

    private void modificarPedido() {
        Intent modPedido = new Intent(VerDetallesPedidoActivity.this, ModificarPedidoActivity.class);
        modPedido.putExtra("idPedido", idPedido);
        startActivity(modPedido);
        finish();
    }

    private void eliminarPedido(DataSnapshot dataSnapshotEliminar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VerDetallesPedidoActivity.this,R.style.DatePickerTheme);
        String mensajeConfirmacionElim = "¿Estás seguro de que quieres eliminar este pedido?";
        builder.setMessage(mensajeConfirmacionElim);
        builder        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataSnapshotEliminar.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //  pedido  eliminado correctamente
                                Log.d("EliminarPedido", "Pedido eliminado correctamente");
                                Toast.makeText(VerDetallesPedidoActivity.this, "Pedido eliminado ", Toast.LENGTH_LONG).show();
                                volverPedidos();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VerDetallesPedidoActivity.this, "Error al eliminar el pedido", Toast.LENGTH_LONG).show();
                                Log.d("EliminarPedido", "Error eliminar pedido: " + e.getMessage());
                                volverPedidos();
                            }
                        });

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        // Crea el AlertDialog y lo muestra
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    /**
     * Buscar el pedido por el id de la ativity anterior
     * @param idPedido id del pedido obtenido en la activity anterior
     */
    private void buscarPedido(String idPedido) {
        // Referencia a la base de datos mediante el id pedido
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("pedidos").child(idPedido);
        Log.d("PedidoBuscar", "Pedido buscado : " + idPedido);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    //Obj Pedido encontrado a partir del Idpedido
                    Pedido pedido = dataSnapshot.getValue(Pedido.class);
                    if (pedido!= null){
                        Log.d("PedidoEncontrado", "Pedido encontrado por id: " + pedido.toString());
                        //Boton eliminar pedido, el listener tiene que estar dentro del ondatachange
                        eliminarPedidoButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                eliminarPedido(dataSnapshot);
                            }
                        });
                        llenarLista(pedido);
                        fechaPedidoDetalleText.setText(pedido.getFecha_pedido().toString());
                        fechaEntregaDetalleText.setText(pedido.getFecha_entrega().toString());
                        cometariosDetalleText.setText(pedido.getComentarios().toString());
                        Locale locale = Locale.US;//Para poner el . como serparador
                        totalDetalleText.setText(String.format(locale,"%.2f",pedido.getPrecio_total()) + "\u20AC");
                        //totalDetalleText.setText(String.valueOf(pedido.getPrecio_total()) + "\u20AC"); todo quitar esto
                        idPedidoTextView.setText(getString(R.string.idPedidoString) + idPedido.substring(3, 7));
                        //Controlar editar el peddio en funcion de si el pedido es editable
                        //Cambiar color y la visibilidad en funcion del estado
                        if (pedido.getEditable() == true){
                            textModPedidoInfo.setBackgroundColor(getResources().getColor(R.color.recogerPedidoColor));
                            textModPedidoInfo.setText(getString(R.string.modPedidoMensaje));
                            layoutEditarPedido.setVisibility(View.VISIBLE);
                        }
                        else {
                            textModPedidoInfo.setBackgroundColor(getResources().getColor(R.color.defPedidoColor));
                            textModPedidoInfo.setText(getString(R.string.noModPedidoMensaje));
                            layoutEditarPedido.setVisibility(View.GONE);
                        }
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores de la base de datos
                Log.e("Error BBDD", "Error al buscar el pedido: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Llenar la lista con los detalles del pedido
     * @param pedido objeto pedido del que se obtienen los detalles
     */
    private void llenarLista(Pedido pedido){
        listaDetalle.setAdapter(new AdaptadorDetallesNoparcel(VerDetallesPedidoActivity.this, R.layout.detalle_pedido_vista, pedido.getDetalles()) {
            @Override
            public void onEntrada(DetallePedidoNoParcel detallePedido, View view) {
                if (detallePedido != null) {
                    TextView nombreRacionDetalle = view.findViewById(R.id.nombreRacionDetalle);
                    TextView cantidadRacionDetalleVistaDetalle = view.findViewById(R.id.cantidadRacionDetalleVistaDetalle);
                    TextView cantidadRacionVistaDetalle = view.findViewById(R.id.cantidadRacionVistaDetalle);
                    TextView precioRacionDetalleVistaDetalle = view.findViewById(R.id.precioRacionDetalleVistaDetalle);
                    ImageView imagenRacion = view.findViewById(R.id.imagenRacion);
                    nombreRacionDetalle.setText(detallePedido.getRacion());
                    cantidadRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getCantidad()));
                    Locale locale = Locale.US;//Para poner el . como serparador
                    precioRacionDetalleVistaDetalle.setText(String.format(locale,"%.2f",detallePedido.getPrecio() * detallePedido.getCantidad()) + "\u20AC");
                    // Utiliza Glide para cargar la imagen desde la URL
                    Glide.with(VerDetallesPedidoActivity.this)
                            .load(URL_FOTOS + detallePedido.getRacion() + URL_SUFIJO)
                            .into(imagenRacion);

                }
            }
        });

    }

    /**
     * Volver pedidos
     */
    private  void volverPedidos() {
        Intent intent = new Intent(VerDetallesPedidoActivity.this, VerPedidoActivity.class);
        startActivity(intent);
        finish();
    }
}