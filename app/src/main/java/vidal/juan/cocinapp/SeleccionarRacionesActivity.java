package vidal.juan.cocinapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SeleccionarRacionesActivity extends AppCompatActivity {

    private ListView lista;
    private Button botonPedir;
    private ArrayList<EncapsuladorEntradas> datos;
    private boolean datosCargados = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cocinaapp-7da53-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference myRef = database.getReference().child("raciones");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realizar_pedido);

        // Referencias a los elementos en activity_realizar_pedido.xml
        lista = findViewById(R.id.lista);
        botonPedir = findViewById(R.id.botonPedir);

        datos = new ArrayList<>();

        // Cargar datos desde Firebase
        cargarDatosFirebase();

        // Configurar listener para el botón Pedir
        botonPedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la cantidad total y el precio total
                int cantidadTotal = obtenerCantidadTotal(datos);
                double precioTotal = obtenerPrecioTotal(datos);
                // Llamada al método para obtener los detalles seleccionados
                ArrayList<DetallePedido> detallesSeleccionados = obtenerDetallesSeleccionados();
                //Comprobar que se seleeciona algo para pasar a la siguiente activity
                if (!detallesSeleccionados.isEmpty()) {
/*
                    //Prueba mostrar en el log lo que se ha seleccionado
                    for (DetallePedido detalle : detallesSeleccionados) {
                        Log.d("DetallesSeleccionados", "Nombre: " + detalle.getNombreRacion() +
                                ", Cantidad: " + detalle.getCantidad() +
                                ", Precio : " + detalle.getPrecio() + "precio total: " + precioTotal);
                    }*/
                    // Iniciar la actividad para escoger fecha; hay que pasar el arraylist
                    //Pasar arraylist  detallesSeleccionados + preciototal a siguiente activity
                    Intent intent = new Intent(SeleccionarRacionesActivity.this, HacerPedidoActivity.class);
                    startActivity(intent);
                }

            }
        });

        // Ocultar el botón Pedir inicialmente
        botonPedir.setVisibility(View.GONE);
    }

    private void cargarDatosFirebase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String tituloEntrada = childSnapshot.getKey();
                        String descripcion = childSnapshot.child("descripcion").getValue(String.class);
                        Long pedidoMax = childSnapshot.child("pedido_max").getValue(Long.class);
                        String precio = childSnapshot.child("precio").getValue(String.class);
                        String stock = childSnapshot.child("stock").getValue(String.class);

                        Log.d("Firebase", "Descripción: " + descripcion + ", Precio: " + precio + ", Pedido Máximo: " + pedidoMax + ", Stock: " + stock);

                        datos.add(new EncapsuladorEntradas(R.drawable.tortilla, tituloEntrada, precio, Long.valueOf(pedidoMax).intValue(), Long.valueOf(stock).intValue()));
                    }

                    // Inicializa tu adaptador después de que se hayan cargado los datos
                    lista.setAdapter(new AdaptadorEntradas(SeleccionarRacionesActivity.this, R.layout.entrada, datos) {
                        @Override
                        public void onEntrada(EncapsuladorEntradas entrada, View view) {
                            if (entrada != null) {
                                TextView titulo_entrada = view.findViewById(R.id.titulo_entrada);
                                TextView precio_entrada = view.findViewById(R.id.precio_entrada);
                                ImageView imagen_entrada = view.findViewById(R.id.imagen);

                                Button botonAnadir = view.findViewById(R.id.botonAnadir);
                                Button botonQuitar = view.findViewById(R.id.botonQuitar);
                                TextView textoCantidad = view.findViewById(R.id.textoCantidad);

                                // Configura los listeners para los botones
                                botonAnadir.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        incrementarCantidad(entrada, textoCantidad);
                                        actualizarBotonPedir();
                                    }
                                });

                                botonQuitar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        decrementarCantidad(entrada, textoCantidad);
                                        actualizarBotonPedir();
                                    }
                                });

                                titulo_entrada.setText(entrada.get_textoTitulo());
                                precio_entrada.setText(entrada.get_Precio());
                                imagen_entrada.setImageResource(entrada.get_idImagen());
                                textoCantidad.setText(String.valueOf(entrada.getCantidadActual()));
                            }
                        }
                    });

                    // Realiza otras operaciones después de que se hayan cargado los datos
                    datosCargados = true;
                    // Actualiza el estado del botónPedir después de cargar los datos
                    actualizarBotonPedir();
                } else {
                    // Manejar el caso en el que el nodo no existe
                    datosCargados = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores en la lectura de datos
                datosCargados = false;
                mostrarMensaje("Error al cargar los datos desde Firebase");
            }
        });
    }

    private void incrementarCantidad(EncapsuladorEntradas entrada, TextView textoCantidad) {
        int cantidadActual = entrada.getCantidadActual();
        int cantidadMaxima = entrada.getCantidadMaxima();
        int stock = entrada.getStock();

        if (cantidadActual < Math.min(cantidadMaxima, stock)) {
            entrada.setCantidadActual(cantidadActual + 1);
            actualizarTextView(entrada, textoCantidad);
        } else {
            if (cantidadActual == cantidadMaxima) {
                mostrarMensaje("Se ha alcanzado el máximo de productos de este tipo por pedido");
            } else {
                mostrarMensaje("Se ha alcanzado el límite de productos disponibles en stock");
            }
        }
    }

    private void mostrarMensaje(String mensaje) {
        Toast toast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    private void decrementarCantidad(EncapsuladorEntradas entrada, TextView textoCantidad) {
        if (entrada.getCantidadActual() > 0) {
            entrada.setCantidadActual(entrada.getCantidadActual() - 1);
            actualizarTextView(entrada, textoCantidad);
        }
    }

    private void actualizarTextView(EncapsuladorEntradas entrada, TextView textoCantidad) {
        textoCantidad.setText(String.valueOf(entrada.getCantidadActual()));
    }

    private void actualizarBotonPedir() {
        // Verificar si hay al menos un producto con cantidad mayor que 0
        boolean mostrarBoton = false;
        for (EncapsuladorEntradas entrada : datos) {
            if (entrada.getCantidadActual() > 0) {
                mostrarBoton = true;
                actualizarTextoBoton();
                break;
            }
        }

        // Mostrar u ocultar el botón Pedir según sea necesario
        botonPedir.setVisibility(mostrarBoton ? View.VISIBLE : View.GONE);
    }

    private int obtenerCantidadTotal(ArrayList<EncapsuladorEntradas> datos) {
        int cantidadTotal = 0;
        for (EncapsuladorEntradas entrada : datos) {
            cantidadTotal += entrada.getCantidadActual();
        }
        return cantidadTotal;
    }

    private double obtenerPrecioTotal(ArrayList<EncapsuladorEntradas> datos) {
        double precioTotal = 0;
        for (EncapsuladorEntradas entrada : datos) {
            precioTotal += entrada.getCantidadActual() * Double.parseDouble(entrada.get_Precio());
        }
        return precioTotal;
    }

    private void actualizarTextoBoton() {
        // Verificar si hay al menos un producto con cantidad mayor que 0
        boolean mostrarBoton = false;
        for (EncapsuladorEntradas entrada : datos) {
            if (entrada.getCantidadActual() > 0) {
                mostrarBoton = true;
                break;
            }
        }

        // Mostrar u ocultar el botón Pedir según sea necesario
        botonPedir.setVisibility(mostrarBoton ? View.VISIBLE : View.GONE);

        // Actualizar el texto del botón con la cantidad total y el precio total
        if (mostrarBoton) {
            int cantidadTotal = obtenerCantidadTotal(datos);
            double precioTotal = obtenerPrecioTotal(datos);
            String textoBoton = "Pedir " + cantidadTotal + " por " + String.format("%.2f", precioTotal) + " €";
            botonPedir.setText(textoBoton);
        }
    }
    //Obtener lista de detalles
    private ArrayList<DetallePedido> obtenerDetallesSeleccionados() {
        ArrayList<DetallePedido> detallesSeleccionados = new ArrayList<>();

        for (EncapsuladorEntradas entrada : datos) {
            int cantidadActual = entrada.getCantidadActual();

            // Solo agregar detalles con cantidad mayor que cero
            if (cantidadActual > 0) {
                DetallePedido detalle = new DetallePedido(
                        entrada.get_textoTitulo(),
                        cantidadActual,
                        Double.parseDouble(entrada.get_Precio())
                );
                detallesSeleccionados.add(detalle);
            }
        }

        return detallesSeleccionados;
    }

}
