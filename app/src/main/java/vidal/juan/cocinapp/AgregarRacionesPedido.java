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

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class AgregarRacionesPedido extends AppCompatActivity {

    private ListView lista;
    private Button addRacionButton,cancelarButton;
    private ArrayList<EncapsuladorEntradas> datos;
    private ArrayList<DetallePedido> detallesActuales;
    private boolean datosCargados = false;
    private final String URL_FOTOS = "https://firebasestorage.googleapis.com/v0/b/cocinaapp-7da53.appspot.com/o/";
    private final String URL_SUFIJO = "?alt=media";
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cocinaapp-7da53-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference myRef = database.getReference().child("raciones");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_raciones);
        Log.d("ActivityLifecycle", "onCreate() SeleccionarRaciones");
        // Referencias a los elementos en activity_realizar_pedido.xml
        lista = findViewById(R.id.lista);
        addRacionButton = findViewById(R.id.botonPedir);
        cancelarButton = findViewById(R.id.cancelarButton);
        datos = new ArrayList<>();
        // Obtener el ArrayList de detalles actuales
        detallesActuales = getIntent().getParcelableArrayListExtra("detallesActuales");
        Log.d("DetallesActuales", "Los detalles que ya estan el la vista de modificar pedido: " + detallesActuales.toString());
        // Cargar datos desde Firebase
        cargarDatosFirebase();

        // Configurar listener para el botón Pedir
        addRacionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la cantidad total y el precio total
                int cantidadTotal = obtenerCantidadTotal(datos);
                double precioTotal = obtenerPrecioTotal(datos);
                // Llamada al método para obtener los detalles seleccionados
                ArrayList<DetallePedido> detallesSeleccionadosAgregados = obtenerDetallesSeleccionados();
                //Comprobar que se seleeciona algo para pasar a la siguiente activity
                if (!detallesSeleccionadosAgregados.isEmpty()) {
                    detallesActuales.addAll(detallesSeleccionadosAgregados);
                    Log.d("DetallesActuales", "resultado suma detalles actuales + nuevos: " + detallesActuales.toString());
                    //Devolver detales seleccionados a actividad anteriror
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra("detallesAgregados", detallesActuales);
                    intent.putExtra("precioTotal", precioTotal);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });
        cancelarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Ocultar el botón Pedir inicialmente
        addRacionButton.setVisibility(View.GONE);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLifecycle", "onDestroy() SeleccionarRaciones");
    }

    private void cargarDatosFirebase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    datos = new ArrayList<>();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String tituloEntrada = childSnapshot.getKey();
                        String descripcion = childSnapshot.child("descripcion").getValue(String.class);
                        Long pedidoMax = childSnapshot.child("pedido_max").getValue(Long.class);
                        String precioPrev = childSnapshot.child("precio").getValue(String.class);
                        //Tratar los decimales del precio
                        // Crea una instancia de DecimalFormat con el formato deseado
                        Log.d("getDecimalPrecio", "Decimal y precio de bbdd" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "precio:  " + precioPrev);
                        Locale locale = Locale.getDefault();
                        Log.d("Locale", "Idioma: " + locale.getLanguage() + ", País: " + locale.getCountry());
                        double precioDecimal = Double.parseDouble(precioPrev);
                        Log.d("getDecimalPrecio", "Precio decimal" + precioDecimal);
                        // Crear DecimalFormatSymbols con el punto como separador decimal
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
                        symbols.setDecimalSeparator('.');
                        DecimalFormat df = new DecimalFormat("#.00", symbols);
                        String precio = df.format(precioDecimal);
                        Log.d("getDecimalPrecio", "Precio decimal depues del format" + precio);

                        String stock = childSnapshot.child("stock").getValue(String.class);
                        String urlImagen = URL_FOTOS + childSnapshot.child("foto").getValue(String.class) + URL_SUFIJO;
                        Log.d("DatosRacion", "Racion: " + tituloEntrada);
                        //Añadir a datos solo las raciones que no existan como detalles actuales
                        // Verificar si la racion ya está en detallesActuales
                        boolean detalleExistente = false;
                        if (detallesActuales != null){
                            for (DetallePedido detalleActual : detallesActuales) {
                                if (detalleActual.getRacion().equals(tituloEntrada)) {
                                detalleExistente = true;
                                break;
                             }
                            }
                            // Si la no está en detallesActuales, agregarlo a la lista datos
                            if (!detalleExistente) {
                                Log.d("DatosRacion", "Racion gregarda : " + tituloEntrada);
                                ///Evitar crasheo al agregar mal raciones en la BBBDD
                                if (urlImagen != null && tituloEntrada != null && descripcion != null && precio != null && pedidoMax != null && stock != null) {
                                    datos.add(new EncapsuladorEntradas(urlImagen, tituloEntrada, descripcion, precio, Long.valueOf(pedidoMax).intValue(), Long.valueOf(stock).intValue()));
                                }

                            }
                        }
                    }

                    // Inicializa tu adaptador después de que se hayan cargado los datos
                    lista.setAdapter(new AdaptadorEntradas(AgregarRacionesPedido.this, R.layout.entrada, datos) {
                        @Override
                        public void onEntrada(EncapsuladorEntradas entrada, View view) {
                            if (entrada != null) {
                                TextView titulo_entrada = view.findViewById(R.id.titulo_entrada);
                                TextView precio_entrada = view.findViewById(R.id.precio_entrada);
                                ImageView imagen_entrada = view.findViewById(R.id.imagen);
                                TextView descripcion = view.findViewById(R.id.descripcion_entrada);
                                Button botonAnadir = view.findViewById(R.id.botonAnadir);
                                Button botonQuitar = view.findViewById(R.id.botonQuitar);
                                TextView textoCantidad = view.findViewById(R.id.textoCantidad);
                                // Utiliza Glide para cargar la imagen desde la URL
                                Glide.with(AgregarRacionesPedido.this)
                                        .load(entrada.getUrlImagen())
                                        .into(imagen_entrada);

                                // Configura los listeners para los botones
                                botonAnadir.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        incrementarCantidad(entrada, textoCantidad);
                                        actualizarBotonAdd();
                                    }
                                });

                                botonQuitar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        decrementarCantidad(entrada, textoCantidad);
                                        actualizarBotonAdd();
                                    }
                                });

                                titulo_entrada.setText(entrada.get_textoTitulo());
                                descripcion.setText(entrada.getDescripcion());
                                precio_entrada.setText(entrada.get_Precio() + "€");
                                textoCantidad.setText(String.valueOf(entrada.getCantidadActual()));
                            }
                        }
                    });

                    // Realiza otras operaciones después de que se hayan cargado los datos
                    datosCargados = true;
                    // Actualiza el estado del botónPedir después de cargar los datos
                    actualizarBotonAdd();
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

    private void actualizarBotonAdd() {
        // Verificar si hay al menos un producto con cantidad mayor que 0
        boolean mostrarBoton = false;
        for (EncapsuladorEntradas entrada : datos) {
            if (entrada.getCantidadActual() > 0) {
                mostrarBoton = true;
                actualizarTextoBoton();
                break;
            }
        }

        // Mostrar u ocultar el botón add según sea necesario
        addRacionButton.setVisibility(mostrarBoton ? View.VISIBLE : View.GONE);
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
        addRacionButton.setVisibility(mostrarBoton ? View.VISIBLE : View.GONE);

        // Actualizar el texto del botón con la cantidad total y el precio total
        if (mostrarBoton) {
            int cantidadTotal = obtenerCantidadTotal(datos);
            double precioTotal = obtenerPrecioTotal(datos);
            String textoBoton = "Añadir " + cantidadTotal + " por " + String.format("%.2f", precioTotal) + " €";
            addRacionButton.setText(textoBoton);
        }
    }
    //Obtener lista de detalles seleccionados nuevos
    private ArrayList<DetallePedido> obtenerDetallesSeleccionados() {
        ArrayList<DetallePedido> detallesSeleccionados = new ArrayList<>();

        for (EncapsuladorEntradas entrada : datos) {
            int cantidadActual = entrada.getCantidadActual();

            // Solo agregar detalles con cantidad mayor que cero
            if (cantidadActual > 0) {
                DetallePedido detalle = new DetallePedido(
                        entrada.get_textoTitulo(),
                        cantidadActual,
                        Double.parseDouble(entrada.get_Precio()),null,null,null,"agregado"
                );
                Log.d("detallesNuevos", "detalleAgregado a al lista en Agregarraciones"+ detalle.toString() );
                detallesSeleccionados.add(detalle);
            }
        }

        return detallesSeleccionados;
    }

}
