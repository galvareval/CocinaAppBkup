package vidal.juan.cocinapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HacerPedidoActivity extends AppCompatActivity {

    private Button cancelNuevopedidoButton,seleccionarFechaEntregaButton,confirmarPedidoButton;
    private ListView listaDetalle;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private FirebaseUser usuarioLogeado ;
    private TextView total,fechaEntregaConfirm;
    private EditText comentariosTextMultiLine2;
    private String fechaEntrega, fechaPedido;
    private String comentarios = "Sin comentarios";
    private ArrayList<DetallePedido> detallesSeleccionados;
    private ArrayList<DetallePedidoNoParcel> detallesSeleccionadosNoParcel = new ArrayList<>();//Iincializar el array list para luego hacer la transformación
    private double precioTotal;
    // Formatear la fecha al formato deseado: aaaa-MM-dd
    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
    //Formatear para guardar hora minutos y segundos
    SimpleDateFormat formatoHoraMinSeg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final int apartirDiasRecoger = 4;//Dias a partir de los cuales se puede recoger Dias definidos por esther ? TODO sacar este dato de BBDD?
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hacer_pedido);
        Log.d("ActivityLifecycle", "onCreate() HacerPedido");
        //referencia a items xml
        cancelNuevopedidoButton = findViewById(R.id.cancelNuevopedidoButton);
        seleccionarFechaEntregaButton = findViewById(R.id.seleccionarFechaEntregaButton);
        confirmarPedidoButton = findViewById(R.id.confirmarPedidoButton);
        listaDetalle = findViewById(R.id.listaDetalle);
        total = findViewById(R.id.total);
        comentariosTextMultiLine2 = findViewById(R.id.comentariosTextMultiLine2);
        fechaEntregaConfirm =  findViewById(R.id.fechaEntregaConfirm);

        //Usuario logeado en la app
         usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();

        // Recuperar detalles seleccionados y precio total de la anterior actividad
        detallesSeleccionados = getIntent().getParcelableArrayListExtra("detallesSeleccionados");
        precioTotal = getIntent().getDoubleExtra("precioTotal", 0.0);

        //Prueba mostrar en el log lo que se ha seleccionado
        for (DetallePedido detalle : detallesSeleccionados) {
            Log.d("DetallesSeleccionadostoinsert", "Nombre: " + detalle.getRacion() +
                    ", Cantidad: " + detalle.getCantidad() +
                    ", Precio : " + detalle.getPrecio() + "precio total: " + precioTotal);
        }
        //Llenar la lista de la vista detalle_pedido_vista.xml  con los detalles obtenidos para mostrarlos como confirmación
        listaDetalle.setAdapter(new AdaptadorDetalles(HacerPedidoActivity.this, R.layout.detalle_pedido_vista, detallesSeleccionados) {
            @Override
            public void onEntrada(DetallePedido detallePedido, View view) {
                if (detallePedido != null) {
                    TextView nombreRacionDetalle = view.findViewById(R.id.nombreRacionDetalle);
                    TextView cantidadRacionDetalleVistaDetalle = view.findViewById(R.id.cantidadRacionDetalleVistaDetalle);
                    TextView cantidadRacionVistaDetalle = view.findViewById(R.id.cantidadRacionVistaDetalle);
                    TextView precioRacionDetalleVistaDetalle = view.findViewById(R.id.precioRacionDetalleVistaDetalle);

                    nombreRacionDetalle.setText(detallePedido.getRacion());
                    cantidadRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getCantidad()));
                    precioRacionDetalleVistaDetalle.setText(String.valueOf (detallePedido.getPrecio()) + "\u20AC");


                }
            }
        });
        //Mostrar el total del pedido; obtenido de la activdad anterior
        total.setText("Total: " + String.valueOf(precioTotal));//Todo formatear mejor esto, redondearlo

        //Evento Botón Seleccionar fecha de entrga
        seleccionarFechaEntregaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Obtener la fecha seleccionada
                obtenerFechaDatepicker();
            }
        });

        //Evento para confirmar el pedido, se muestra una vez obtenida la fecha, y que se graba en la BBDD
        confirmarPedidoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Insertar en la BBDD el nuevo pedido a partir de los datos de la interfaz
                insertarPedidoParaUsuario();
            }
        });


        //Volver a la pantalla principal
        cancelNuevopedidoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HacerPedidoActivity.this,"Pedido cancelado" , Toast.LENGTH_LONG).show();
                volverPprincipal();
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLifecycle", "onDestroy() HacerPedido");
    }

    /**
     * Metodo para obener la fecha seleccionada en el datepicker; Tiene restricion mediante la CONSTANTE a paritr dias recoge
     * Se muestran los dias siempre a partir de esta constante. Tabién se obtiene la fecha del dia actual formateada
     */
    private void obtenerFechaDatepicker() {

        //Obtener referencias al dia de hoy
        final Calendar calendar =  Calendar.getInstance();
        final int ano = calendar.get(Calendar.YEAR);
        final int mes = calendar.get(Calendar.MONTH);
        final int dia = calendar.get(Calendar.DAY_OF_MONTH);
        //Formatear la fecha a dia de hoy para pasarla al insert
        fechaPedido = formatoHoraMinSeg.format(calendar.getTime());
        DatePickerDialog datePickerDialog = new DatePickerDialog(this ,R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //Setear calendar con la fecha seleccionada
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                // Validar si la fecha seleccionada es igual al día de hoy
                Calendar fechaSeleccionada = Calendar.getInstance();
                fechaSeleccionada.set(year, month, day);
                //Que la fecha seleccionada no sea el dia de hoy
                if (fechaSeleccionada.get(Calendar.YEAR) == ano &&
                        fechaSeleccionada.get(Calendar.MONTH) == mes &&
                        fechaSeleccionada.get(Calendar.DAY_OF_MONTH) == dia) {
                    Toast.makeText(HacerPedidoActivity.this, "Seleccione una fecha diferente al día de hoy", Toast.LENGTH_SHORT).show();
                }else {
                    String fechaFormateada = formato.format(calendar.getTime());
                    //Que hacer cuando se seleccione la fecha
                    Toast.makeText(HacerPedidoActivity.this, "Fecha de entrega: " + fechaFormateada, Toast.LENGTH_LONG).show();
                    fechaEntrega = fechaFormateada;
                    Log.d("FechaSel", "FechaSel: " + fechaEntrega);
                    Log.d("FechaPedido", "FechaPedido: " + fechaPedido);
                    fechaEntregaConfirm.setText(fechaEntrega);
                    confirmarPedidoButton.setVisibility(View.VISIBLE);
                }
            }
        }, ano, mes, dia);
        // Restricción para que no se pueda seleccionar el día de hoy
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        //Restriccion de fines de semana
        datePickerDialog.getDatePicker().init(ano, mes, dia, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Verificar si la fecha seleccionada es fin de semana
                calendar.set(year, monthOfYear, dayOfMonth);
                int finde = calendar.get(Calendar.DAY_OF_WEEK);

                // Verificar si la fecha seleccionada es sabado o domingo
                if (finde == Calendar.SATURDAY || finde == Calendar.SUNDAY) {
                    // Si es un fin de semana, error
                    Toast.makeText(getApplicationContext(), "No se pueden seleccionar fines de semana", Toast.LENGTH_SHORT).show();

                    // Restablecer la fecha seleccionada al dia a partir del cual se puede hacer el pedido
                    datePickerDialog.getDatePicker().updateDate(ano, mes, dia);
                }
            }
        });

        //Restriccion de fecha
        Calendar calendarioMin = Calendar.getInstance();
        //Apartir de los dias que diga ESTHER se puede recoger el pedido todo esto se hace con una constante local; habria que hacerlo de BBDD para que sea mas sencillo de configurar
        calendarioMin.add(Calendar.DAY_OF_MONTH, + apartirDiasRecoger);
        //Setear el dia minimo
        datePickerDialog.getDatePicker().setMinDate(calendarioMin.getTimeInMillis() - 1000);
        //TODO hace falta setear dia máximo?
        datePickerDialog.show();


    }

    /**
     * Para volver a la ventana principal
     */
    private  void volverPprincipal() {
        /*Intent intent = new Intent(HacerPedidoActivity.this, PantallaPrincipalActivity.class);
        startActivity(intent);*/
        finish();
    }


    /**
     * /Método para insertar el pedido de un usuario
     *
     */
    private  void insertarPedidoParaUsuario( ) {

            //BBDD referencia para el insert
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // ID para el pedido
            String nuevoPedidoId = databaseReference.child("pedidos").push().getKey();
            // ID de usuario para el pedido
            String userId = usuarioLogeado.getUid();
            //La fecha seleccionada y la fecha de pedido ya han sido obtenidas en la activity; fechaPedido y fechaEntrega
            //Los comentarios del pedido
            comentarios = comentariosTextMultiLine2.getText().toString();
            //Crear el pedido a partir de los datos seleecionados
            //Pasar los detalles a un objeto que no implemente parcelable para que no inserte stability 0 en firebase
            transFormNoParcel();
            //Crear el bojeto pedido con los datos;El estado predeterminado al hacer un pedido es : Preparar
            Pedido nuevoPedido = new Pedido(comentarios,detallesSeleccionadosNoParcel,"preparar",fechaPedido,fechaEntrega,precioTotal,userId);
            //Log para ver pedido
            Log.d("NuevoPedido", "Pedido: " + nuevoPedido.toString());
            // Insertar el nuevo pedido en la colección de pedidos verificando si ha ido bien o no
            databaseReference.child("pedidos").child(nuevoPedidoId.substring(1)).setValue(nuevoPedido).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // DATO Agregado EN BBDD
                            Toast.makeText(HacerPedidoActivity.this,"Pedido registrado "  , Toast.LENGTH_LONG).show();
                            //Actualizar el stock según los detalles del pedido
                            actualizarStock();
                            //volver a la ventana principal
                            volverPprincipal();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HacerPedidoActivity.this,"ERROR No se ha registrado el pedido "  , Toast.LENGTH_LONG).show();
                        }
                    });;

            //Insertar en indices para las busqeudas sencillas--TODO definir indices bien,
        }

    /**todo Esto pueder ser una interfaz
     * Método para transformar un detalle de pedido de la clase DetallePedido en un objeto exactamente igual de la clase DetallePedidoNoParcel pero sin que implemente la interfaz pacelable
     * Previamente usada para pasar los detalles entre activity, de no hacerlo se pasa el campo stability = 0 ya que firebase usa parcelable al insertar el objeto y no se puede ignorar pasarlo
     * La solución es crear un nuevo objeto exactamente igual sin implementar parcelable
     */
    private void transFormNoParcel() {
        for (DetallePedido detalle : detallesSeleccionados) {
            //Cada objeto de la lista detallesSeleccionados pasarlo al arraylist DetallePedidoNoParcel exactamente igual a como estaba
            detallesSeleccionadosNoParcel.add(new DetallePedidoNoParcel(detalle.getRacion(), detalle.getCantidad(), detalle.getPrecio()));
        }
        Log.d("Transformación", "detallesSeleccionadosNoParcel: " + detallesSeleccionadosNoParcel.toString());
    }

    /**
     * Método para actualizar el stock una vez hecho el pedido TODO quiza haya que usarlo en modificar-> pasar a interfaz para todos los sitios en los que se vaya a usar
     */
    private void actualizarStock() {
        //Referencia a raciones en la bbdd
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("raciones");
        //De cada detalle seleccionado hay que obtener el nombre de la racion y la cantidad para actulizarlos
        for (DetallePedidoNoParcel detalle : detallesSeleccionadosNoParcel) {
            final String nombreRacion = detalle.getRacion();
            final int cantidadPedida = detalle.getCantidad();

            // Obtener el stock actual de la ración
            databaseReference.child(nombreRacion).child("stock").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Obtener el stock actual
                    int stockActual = Integer.parseInt(task.getResult().getValue(String.class));

                    // Calcular el nuevo stock restando la cantidad pedida de ese detalle
                    int nuevoStock = stockActual - cantidadPedida;

                    // Actulizar, setear el nuevo stock despues de hacer el pedido de ese detallee
                    databaseReference.child(nombreRacion).child("stock").setValue(Integer.toString(nuevoStock)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Prueba en LOG
                            Log.d("ActualizarStock", "Stock actualizado " + nombreRacion + ". Nuevo stock: " + nuevoStock);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Prueba en LOG
                            Log.e("ActualizarStock", "Error al actualizar el stock " + nombreRacion, e);
                        }
                    });
                } else {
                    Log.e("ActualizarStock", "Error al obtener el stock para " + nombreRacion, task.getException());
                }
            });
        }
    }

}