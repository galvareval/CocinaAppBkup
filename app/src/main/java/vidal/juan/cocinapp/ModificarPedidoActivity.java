package vidal.juan.cocinapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModificarPedidoActivity extends AppCompatActivity {

    private Button volverDetallesPedidosButton,confirmModPedidoButton,seleccionarFechaEntregaButton,addRacionButton;
    private ListView listaDetalleMod = null;
    private TextView fechaPedidoDetalleTextMod,fechaEntregaModTextview,cometariosDetalleTextMod,totalDetalleTextMod,idPedidoModTextView;
    private String idPedido;
    private double precioTotalPedido = 0;
    static final int REQUEST_CODE = 1;

    private String nuevaFechaEntrega, fechaPedido;
    // Formatear la fecha al formato deseado: aaaa-MM-dd
    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
    //Formatear para guardar hora minutos y segundos
    SimpleDateFormat formatoHoraMinSeg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final int apartirDiasRecoger = 4;//Dias a partir de los cuales se puede recoger Dias definidos por esther ? TODO sacar este dato de BBDD?
    private ArrayList<DetallePedido> detallesNuevosAgregados;
    private ArrayList<DetallePedidoNoParcel> detallesNuevosAgregadosNoParcel = null;//Iincializar el array list para luego hacer la transformación
    private double precioTotalDeNuevoAgregado = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_pedido);
        Log.d("ActivityLifecycle", "onCreate() ModificarPedido");
        //referencia a items xml
        volverDetallesPedidosButton = findViewById(R.id.volverDetallesPedidosButton);
        confirmModPedidoButton = findViewById(R.id.confirmModPedidoButton);
        addRacionButton = findViewById(R.id.addRacionButton);
        seleccionarFechaEntregaButton = findViewById(R.id.seleccionarFechaEntregaButton);
        listaDetalleMod = findViewById(R.id.listaDetalleMod);
        fechaPedidoDetalleTextMod = findViewById(R.id.fechaPedidoDetalleTextMod);
        fechaEntregaModTextview = findViewById(R.id.fechaEntregaModTextview);
        cometariosDetalleTextMod = findViewById(R.id.cometariosDetalleTextMod);
        totalDetalleTextMod = findViewById(R.id.totalDetalleTextMod);
        idPedidoModTextView  = findViewById(R.id.idPedidoModTextView);

        //Funcionalidad botones
        //Volver a la pantalla principal
        volverDetallesPedidosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volverDetallePedido();

            }
        });

        //Seleccionar nueva fecha pedido
        seleccionarFechaEntregaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerFechaDatepicker();
            }
        });

        // Recuperar id pedido del pedido seleccionado en la anterior actividad
        idPedido = getIntent().getStringExtra("idPedido");
        //Log
        Log.d("idPedidoRecib", "Pedido: " + idPedido);
        // Buscar y mostrar los detalles del pedido a partir de id
        buscarPedido(idPedido);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Obtener el ArrayList de detalles seleccionados
                detallesNuevosAgregados = data.getParcelableArrayListExtra("detallesSeleccionados");
                // Obtener el precio total
                precioTotalDeNuevoAgregado = data.getDoubleExtra("precioTotal", 0.0);
                Log.d("detallesNuevos", "detalles nuevos orig."+ detallesNuevosAgregados.toString() + "precio total: " + precioTotalDeNuevoAgregado);
                //Trasformar la lista de detalles seleecionados en no parcelabre; si no se agrega un 0 en la bbd por el consutructor que usa firebase, usa parcelable el predeterminado que envia el staturs = 0
                transFormNoParcel();
                //Para forzar el onData cahnge y que se regenere la vista
                buscarPedido(idPedido);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLifecycle", "onDestroy() ModificarPedido");
    }
    /**
     * Buscar el pedido por el id de la ativity anterior
     * @param idPedido id del pedido obtenido en la activity anterior
     */
    private void buscarPedido(String idPedido) {

        // Referencia a la base de datos mediante el id pedido
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("pedidos").child(idPedido);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshotPedido) {
                //Obj Pedido encontrado a partir del Idpedido
                Pedido pedido = dataSnapshotPedido.getValue(Pedido.class);
                if(pedido != null){
                    Log.d("ModPedidoPed", "Pedido al inicar modificar pedido: "+ pedido.toString());
                    //Valores del pedido a la vista
                    fechaPedidoDetalleTextMod.setText(pedido.getFecha_pedido().toString());
                    fechaEntregaModTextview.setText( pedido.getFecha_entrega().toString());
                    cometariosDetalleTextMod.setText(pedido.getComentarios().toString());
                    totalDetalleTextMod.setText(String.valueOf(pedido.getPrecio_total()) + "\u20AC" );
                    idPedidoModTextView.setText(getString(R.string.idPedidoString) + idPedido.substring(3,7));
                    //Agregar al objeto pedido el stock,cantidadMax, y precio de la racion en cada detalle del pedido
                    //Si existen nuevos pedidos agregarlos al pedido
                    if (detallesNuevosAgregadosNoParcel !=null)
                    {
                        // Lista para almacenar los detalles que se van a agregar
                        List<DetallePedidoNoParcel> detallesAgregados = new ArrayList<>();

                        for (DetallePedidoNoParcel detalleNuevo : detallesNuevosAgregadosNoParcel) {
                            boolean encontrado = false;
                            for (DetallePedidoNoParcel detalleOriginal : pedido.getDetalles()) {
                                if (detalleNuevo.getRacion().equals(detalleOriginal.getRacion())) {
                                    encontrado = true;
                                    break;
                                }
                            }
                            if (!encontrado) {
                                detalleNuevo.setStockOriginal("agregado"); // Para luego modificar el stock del que parte el poder modificar
                                detallesAgregados.add(detalleNuevo);
                                double precioAnt = pedido.getPrecio_total();
                                pedido.setPrecio_total(precioAnt + detalleNuevo.getPrecio());
                                Log.d("detallesNuevos", "Precio total nuevo." + pedido.getPrecio_total());
                                totalDetalleTextMod.setText(String.valueOf(pedido.getPrecio_total()) + "\u20AC");
                            }
                        }

                        // Agregar los detalles de la lista detalles agregados al pedido
                        pedido.getDetalles().addAll(detallesAgregados);

                        Log.d("ModPedidoPed", "Pedido al añdir raciones: "+ pedido.toString());
                        //Log.d("detallesNuevos", "Nuevas Raciones."+ pedido.getDetalles().toString());
                    }
                    final int[] racionesRecuperadas = {0};
                    for (DetallePedidoNoParcel detalle : pedido.getDetalles()) {
                        //Encontrar la racion en la BBDD para cada detalle
                        DatabaseReference racionRef = FirebaseDatabase.getInstance().getReference().child("raciones").child(detalle.getRacion());
                        ValueEventListener valueEventListenerRacion;
                        valueEventListenerRacion= new ValueEventListener() {
                            @Override
                            public void onDataChange( DataSnapshot dataSnapshotRacion) {
                                //Crear objeto racion a partir de datos de BBDD
                                Racion racion = dataSnapshotRacion.getValue(Racion.class);
                                if (racion != null) {
                                    //Pasar el stock,precioRacion,cantidadMaximaRacion al detalle del pedido
                                    detalle.setPrecioRacion(racion.getPrecio());
                                    detalle.setPedidoMaxRacion(String.valueOf(racion.getPedido_max()));
                                    //Si el detalle es agregado hay que recalcular el stock original
                                        if(detalle.getStockOriginal() != null){
                                            int cantidadAgregadaRacion = detalle.getCantidad();
                                            int sotkcBdd = Integer.parseInt(racion.getStock());
                                            int stockDepuesAgregado = sotkcBdd-cantidadAgregadaRacion;
                                            detalle.setStockOriginal(String.valueOf(stockDepuesAgregado));
                                            detalle.setStockRacion(String.valueOf(stockDepuesAgregado));
                                        }else{
                                            detalle.setStockOriginal(racion.getStock());
                                            detalle.setStockRacion(racion.getStock());
                                        }

                                    Log.d("AddRacionADetalle", "ADD cant,stock y preico orig." + detalle.toString());
                                    racionesRecuperadas[0]++;
                                    // Verificar si se han recuperado todas las raciones
                                    if (racionesRecuperadas[0] == pedido.getDetalles().size()) {
                                        llenarLista(pedido);
                                        //Boton Modificar el pedido

                                    }
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                            //Litener de confirmar
                        };
                        racionRef.addValueEventListener(valueEventListenerRacion);

                        Log.d("Conex", "Conex"+ racionRef.toString());
                    }
                    //Funcionalidad confirmar pedido
                    confirmModPedidoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(pedido.getEditable() == true) {
                                Log.d("editable", "Edicion = "+ pedido.getEditable());
                                AlertDialog.Builder builder = new AlertDialog.Builder(ModificarPedidoActivity.this, R.style.DatePickerTheme);
                                String mensajeConfirmacionMod = "¿Estás seguro de que quieres modificar este pedido?";
                                String mensajeConfirmacionElim = "¿Estás seguro de que quieres eliminar este pedido?";
                                String totalString = totalDetalleTextMod.getText().toString();

                                if (Double.parseDouble(totalString.substring(0, totalString.length() - 1)) == 0)

                                    builder.setMessage(mensajeConfirmacionElim);

                                else
                                    builder.setMessage(mensajeConfirmacionMod);
                                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                confirmModificarPedido(pedido, dataSnapshotPedido);

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
                            else{
                                Toast.makeText(ModificarPedidoActivity.this, "Este pedido ya no es editable", Toast.LENGTH_SHORT).show();
                                volverPprincipal();
                            }
                        }
                    });
                    //Funcionalidad agregarNuevaRacion
                    addRacionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            seleccionarNuevasRacions();
                        }
                    });

                }
                else {
                    Toast.makeText(ModificarPedidoActivity.this, "No se ha encontrado el pedido", Toast.LENGTH_SHORT).show();
                    volverPprincipal();
                }
            }//Fin on datachange Pediddos
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores de la base de datos
                Log.e("Error BBDD", "Error al buscar el pedido: " + databaseError.getMessage());
            }
        });

    }

    private void seleccionarNuevasRacions() {
        Intent intentSeleccionarNuevasRaciones = new Intent(ModificarPedidoActivity.this, AgregarRacionesPedido.class);
        startActivityForResult(intentSeleccionarNuevasRaciones, REQUEST_CODE);
    }
    private void transFormNoParcel() {
        detallesNuevosAgregadosNoParcel = new ArrayList<>();
        for (DetallePedido detalle : detallesNuevosAgregados) {
            //Cada objeto de la lista detallesSeleccionados pasarlo al arraylist DetallePedidoNoParcel exactamente igual a como estaba
            detallesNuevosAgregadosNoParcel.add(new DetallePedidoNoParcel(detalle.getRacion(), detalle.getCantidad(), detalle.getPrecio()));
        }
        Log.d("Transformación", "detallesNuevosAgregadosNoParcel: " + detallesNuevosAgregadosNoParcel.toString());
    }
    /**
     * Llenar la lista con los detalles del pedido
     * @param pedido objeto pedido del que se obtienen los detalles
     */
    private void llenarLista(Pedido pedido ){
        Log.d("ExecLlenarLista", "Llenar lista ejecutado: " );
        listaDetalleMod.setAdapter(new AdaptadorDetallesNoparcel(this, R.layout.detealle_pedido_mod_vista, pedido.getDetalles()) {
            @Override
            public void onEntrada(DetallePedidoNoParcel detallePedido, View view) {
                if (detallePedido != null) {
                    //Componentes de la vista que muestra el detalle del pediddo
                    Log.d("ExecDetalleAddLista", "DetalleAñadido " + detallePedido.toString() );
                    TextView nombreRacionDetalle = view.findViewById(R.id.nombreRacionDetalle);
                    TextView cantidadRacionDetalleVistaDetalle = view.findViewById(R.id.cantidadRacionDetalleVistaDetalle);
                    TextView precioRacionDetalleVistaDetalle = view.findViewById(R.id.precioRacionDetalleVistaDetalle);
                    Button modDetalleBotonQuitar,modDetalleBotonAnadir;
                    modDetalleBotonQuitar = view.findViewById(R.id.modDetalleBotonQuitar);
                    modDetalleBotonAnadir = view.findViewById(R.id.modDetalleBotonAnadir);
                    //Logs
                    /*Log.d("RacionNombre", "Datos racion buscada : " + detallePedido.getRacion());
                    Log.d("detallesAlaLista", "Datos introducidos a la lista : " + pedido.getDetalles().toString());
                    Log.d("detallesAlaLista", "Tamaño de la lista : " + pedido.getDetalles().size());*/
                    //Valores inciales de la vista de la lista
                    nombreRacionDetalle.setText(detallePedido.getRacion());
                    cantidadRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getCantidad()));
                    precioRacionDetalleVistaDetalle.setText(String.valueOf (detallePedido.getPrecio()) + "\u20AC");
                    //Listeners para los botones
                    //Contador
                    final int[] modCantidad = {0};
                    final int[] stockOrig ={0};
                    //Stock original del detalle
                    stockOrig[0] = Integer.parseInt(detallePedido.getStockOriginal());
                    //Boton añadir
                    modDetalleBotonAnadir.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("ExecRacionEnStock", "Stock al hacer + " + detallePedido.getRacion() + ": " + detallePedido.getStockRacion());
                            //El precio de una racion para añadir o quitar al total
                            double precioUnaracion = Double.parseDouble(detallePedido.getPrecioRacion());
                            int cantidadActual = detallePedido.getCantidad();
                            String cantidadMaxima = detallePedido.getPedidoMaxRacion();
                            //int stock = Integer.parseInt(racion.getStock());

                            Log.d("ExecRaStockAntes+", "Stock antes de hacer click en + " + detallePedido.getRacion() + ": " + detallePedido.getStockRacion());
                            Log.d("ValorModCantidadA", "Valor de mod cantidad Antes de if: " + modCantidad[0] );
                            if (modCantidad[0] < Math.min(Integer.parseInt(cantidadMaxima), stockOrig[0])) {
                                //Aumentar las veces que se dio a +
                                modCantidad[0] += 1;
                                //Modificar el stock
                                int stock = Integer.parseInt(detallePedido.getStockRacion());
                                //Disminuir 1 de sotck
                                detallePedido.setStockRacion(String.valueOf(stock- 1));
                                //Aumentar la cantidad en 1 del pedido
                                detallePedido.setCantidad(cantidadActual + 1);
                                Log.d("ValorModCantidadB", "Valor de mod cantidad depues de if: " + modCantidad[0] );
                                Log.d("ExecRaStockdesp+", "Stock despues de hacer click en + " + detallePedido.getRacion() + ": " + detallePedido.getStockRacion());
                                //Mostrar la actu en la view
                                cantidadRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getCantidad()));
                                double nuevoPrecio = Double.parseDouble(detallePedido.getPrecioRacion()) * (detallePedido.getCantidad());
                                Log.d("Nuevoprecio", "Nuevo precio:  " + nuevoPrecio);
                                //Actualizar el precio del pedido
                                detallePedido.setPrecio(nuevoPrecio);
                                //precioRacionActu[0] = precioUnaracionMas;
                                //Mostrar actu del precio en la view
                                precioRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getPrecio()) + "\u20AC");
                                //Precio total aterior
                                double precioAnt;
                                //String del precio total actual en la vista general, hayq que quitar el simbolo $ con un substring
                                String totalStringAnt = totalDetalleTextMod.getText().toString();
                                precioAnt = Double.parseDouble(totalStringAnt.substring(0, totalStringAnt.length() - 1));
                                Locale locale = Locale.US;//Para poner el . como serparador
                                totalDetalleTextMod.setText(String.format(locale,"%.2f", precioAnt + precioUnaracion) + "\u20AC");
                                //todo quitar Log.e("stockdetalles", "Pulsar boton detalles mod."+ pedido.toString());

                            } else {
                                if (modCantidad[0] == Integer.parseInt(cantidadMaxima)) {
                                    Toast.makeText(ModificarPedidoActivity.this, "Se ha alcanzado el máximo de productos de este tipo por pedido", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(ModificarPedidoActivity.this, "Se ha alcanzado el límite de productos disponibles en stock", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    });
                    // Boton quitar
                    modDetalleBotonQuitar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //El precio de una racion para añadir o quitar al total
                            double precioUnaracion = Double.parseDouble(detallePedido.getPrecioRacion());
                            if (detallePedido.getCantidad() > 0) {
                                //Disminuir las veces que se dio a boton
                                modCantidad[0] -= 1;
                                int stock = Integer.parseInt(detallePedido.getStockRacion());
                                detallePedido.setStockRacion(String.valueOf(stock + 1));
                                Log.d("StockActual-", "Stock del producto : " + detallePedido.getRacion() + " " + (detallePedido.getStockRacion()));
                                //Disminuir cantidad mostrada
                                detallePedido.setCantidad(detallePedido.getCantidad() - 1);
                                if (detallePedido.getCantidad() == 0) {
                                    Toast.makeText(ModificarPedidoActivity.this, detallePedido.getRacion() + " se eliminará del pedido", Toast.LENGTH_SHORT).show();
                                }
                                //Mostrar la actu en la view
                                cantidadRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getCantidad()));
                                //Actualizar el precio del pedido
                                detallePedido.setPrecio(Double.parseDouble(detallePedido.getPrecioRacion()) * (detallePedido.getCantidad()));
                                precioRacionDetalleVistaDetalle.setText(String.valueOf(detallePedido.getPrecio()) + "\u20AC");//Mostrar actu del precio en la view
                                //Precio total anterior original
                                double precioAnt;
                                //String del precio total actual en la vista geneeral, hayq que quitar el simbolo $ con un substring
                                String totalStringAnt = totalDetalleTextMod.getText().toString();
                                precioAnt = Double.parseDouble(totalStringAnt.substring(0, totalStringAnt.length() - 1));
                                Locale locale = Locale.US;//Para poner el . como serparador
                                totalDetalleTextMod.setText(String.format(locale,"%.2f", precioAnt - precioUnaracion) + "\u20AC");


                            }
                        }
                    });
                }
            }
        });//Fin llenar lista
        Log.d("ExecTamLista", "tamaño de la lista: " + listaDetalleMod.getAdapter().getCount());
        //((BaseAdapter) listaDetalleMod.getAdapter()).notifyDataSetChanged();
    }




    /**
     * Volver Detalles del pedido
     */
    private  void volverDetallePedido() {
        Intent intentDetallesPedido = new Intent(ModificarPedidoActivity.this, VerDetallesPedidoActivity.class);
        intentDetallesPedido.putExtra("idPedido", idPedido);//Pasar el id del detalle del peddido para volver a donde estaba
        startActivity(intentDetallesPedido);
        finish();

    }
    /**
     * Volver Pantalla principal
     */
    private void volverPprincipal() {
        /*Intent intentPprincipal = new Intent(ModificarPedidoActivity.this, PantallaPrincipalActivity.class);*/

        finish();

    }

    /**
     * Metodo para obener la fecha seleccionada en el datepicker; Tiene restricion mediante la CONSTANTE a paritr dias recoge
     * Se muestran los dias siempre a partir de esta constante.
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
                    Toast.makeText(ModificarPedidoActivity.this, "Seleccione una fecha diferente al día de hoy", Toast.LENGTH_SHORT).show();
                }else {
                    String fechaFormateada = formato.format(calendar.getTime());
                    //Que hacer cuando se seleccione la fecha
                    Toast.makeText(ModificarPedidoActivity.this, "Nueva fecha de entrega: " + fechaFormateada, Toast.LENGTH_LONG).show();
                    nuevaFechaEntrega = fechaFormateada;
                    Log.d("FechaSel", "FechaSel: " + nuevaFechaEntrega);
                    Log.d("FechaPedido", "FechaPedido: " + fechaPedido);
                    fechaEntregaModTextview.setText(nuevaFechaEntrega);
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
    private void confirmModificarPedido( Pedido pedido,DataSnapshot dataSnapshotPedido) {
        //Actulizar el stock con los nuevos datos

        //Contador para actualizar pedido una vez se hallan modificado todas las raciones del pedido
        final int[] contadorActuRaciones = {0};
        //Recorrer los detalles
        for (DetallePedidoNoParcel detalleActu: pedido.getDetalles())
        {
            //Obtener datos de la racion por nombre de la racion
            DatabaseReference datarefActuRacion;
            datarefActuRacion = FirebaseDatabase.getInstance().getReference().child("raciones").child(detalleActu.getRacion());
            datarefActuRacion.child("stock").setValue(detalleActu.getStockRacion()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {//Cuando la racion se halla actualizado correctamente->Aumentar el contador de raciones actualizadas
                    Log.d("ActuRacion", "Racion " + detalleActu.getRacion() + "actualizada");
                    contadorActuRaciones[0]++;//Aumentar el contador cuando se actulice la racoin en la BBDD
                    Log.d("ActuRacion", "Valor del contador " + contadorActuRaciones[0]);
                    if (contadorActuRaciones[0] == pedido.getDetalles().size()) {//Cuando se hallan actualizado todas las raciones->Actualizar el pedido con los nuevos datos
                        //Actualizar el pedido
                        //Datos modificados en la vista, pasarlos al pedido
                        Log.d("ActuRacion", "Entro en update pedido Valor del contador" + contadorActuRaciones[0]);
                        String totalString = totalDetalleTextMod.getText().toString();
                        pedido.setPrecio_total((Double.parseDouble(totalString.substring(0, totalString.length() - 1))));
                        pedido.setComentarios(cometariosDetalleTextMod.getText().toString());
                        pedido.setFecha_entrega(fechaEntregaModTextview.getText().toString());
                        //List para detalles nuevos sin datos del stock
                        List<DetallePedidoNoParcel> detallesSinDatosStock = new ArrayList<>();
                        for (DetallePedidoNoParcel detalleConDatosStock : pedido.getDetalles()) {
                            // Crear un nuevo objeto DetallePedidoNoParcel sin incluir los campos precioRacion, pedidoMaxRacion y stockRacion
                            String racionActu = detalleConDatosStock.getRacion();
                            int cantidadActu = detalleConDatosStock.getCantidad();
                            double precioActu = detalleConDatosStock.getPrecio();
                            DetallePedidoNoParcel detalleSinDatosStock = new DetallePedidoNoParcel(
                                    racionActu,
                                    cantidadActu,
                                    precioActu
                            );
                            Log.d("ListaSinStock", "Detalles sin stock " + detallesSinDatosStock.toString());
                            //Añadir a la lista los que no esten vacios
                            if(detalleSinDatosStock.getCantidad()>0)
                                detallesSinDatosStock.add(detalleSinDatosStock);
                        }
                        //Pasar la lista al pedido

                        Log.d("ActuPedidoSinStock", "Detalles sin stock " + detallesSinDatosStock.toString());
                        //Crear un Map con lo que se tiene que actulizar del pedido
                        Map<String, Object> actuPedido = new HashMap<>();
                        actuPedido.put("comentarios", pedido.getComentarios());
                        actuPedido.put("precio_total", pedido.getPrecio_total());
                        actuPedido.put("fecha_entrega", pedido.getFecha_entrega());
                        actuPedido.put("detalles", detallesSinDatosStock);
                        // Actualizar los campos en la base de datos
                        Log.d("ActuPedido", "Pedido antes de añadir listener " + pedido.toString());

                        if((pedido.getPrecio_total()) > 0)
                        {
                            modificarPedido(dataSnapshotPedido, actuPedido);

                        }
                        else{
                            eliminarPedido(dataSnapshotPedido);
                        }

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("ActuRacion", "Error al escribir en la base de datos: " + e.getMessage());
                    volverDetallePedido();
                }
            });
        }
    }
    private void modificarPedido(DataSnapshot dataSnapshotPedidoActu, Map actuPedido) {

        dataSnapshotPedidoActu.getRef().updateChildren(actuPedido).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // El pedido se ha eliminado correctamente
                Log.d("ModPedido", "Pedido actulizado correctamente");
                Toast.makeText(ModificarPedidoActivity.this, "Pedido actulizado ", Toast.LENGTH_LONG).show();
                volverDetallePedido();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(ModificarPedidoActivity.this, "Error al eliminar el pedido", Toast.LENGTH_LONG).show();
                Log.d("ModPedido", "Error al actulizar pedido: " + e.getMessage());
                volverPprincipal();
            }
        });
    }

    private void eliminarPedido(DataSnapshot dataSnapshotEliminar) {
        dataSnapshotEliminar.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //  pedido  eliminado correctamente
                Log.d("EliminarPedido", "Pedido eliminado correctamente");
                Toast.makeText(ModificarPedidoActivity.this, "Pedido eliminado ", Toast.LENGTH_LONG).show();
                volverPprincipal();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ModificarPedidoActivity.this, "Error al eliminar el pedido", Toast.LENGTH_LONG).show();
                Log.d("EliminarPedido", "Error eliminar pedido: " + e.getMessage());
                volverPprincipal();
            }
        });

    }

}