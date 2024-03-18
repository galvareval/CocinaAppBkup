package vidal.juan.cocinapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DecimalFormatSymbols;

public class PantallaPrincipalActivity extends AppCompatActivity {
    //Items del xml
    TextView usuarioLogeadoTextView;
    Button editarRegistroButton, cerrarSesionButton, hacerPedidoButton,verPedidosButton,verHistoricoButton;
    private FirebaseUser usuarioLogeado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);
        Log.d("ActivityLifecycle", "onCreate() PantallaPrincipal");
        //ver si usa . o coma todo quitar esto, es una prueba
        Log.d("getDecimal", "Decimal" +  DecimalFormatSymbols.getInstance().getDecimalSeparator());
        //Ref items del xml
        usuarioLogeadoTextView = findViewById(R.id.usuarioLogeadoTextView);
        editarRegistroButton = findViewById(R.id.editarRegistroButton);
        cerrarSesionButton = findViewById(R.id.cerrarSesionButton);
        hacerPedidoButton = findViewById(R.id.hacerPedidobutton);
        verPedidosButton = findViewById(R.id.verPedidosButton);
        verHistoricoButton = findViewById(R.id.verHistoricoButton);
        //FIREBASE; usuario logeado
        usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();
        // Obtener el mail del usario logeado, todo Cambiar por el nombre de usuario con un bienvenidos?
        /*if (usuarioLogeado != null ){
            usuarioLogeadoTextView.setText("Bienvenido " + "/n" + usuarioLogeado.getEmail());
        }*/

        //Accion del boton prueba para editar el registro
        tokenAppp();
        editarRegistroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarRegistro();
            }
        });

        //Accion del boton nuevo pedido
        hacerPedidoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hacerPedido();
            }
        });

        //Accion del boton ver pedidos
        verPedidosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verPedidos();
            }
        });

        // Accion del boton de logout
        cerrarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            FirebaseAuth.getInstance().signOut();
            Intent volverLoginIntent = new Intent(PantallaPrincipalActivity.this, MainActivity.class);
            startActivity(volverLoginIntent);
            finish();
            }
        });



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLifecycle", "onDestroy() Pantalla principal");
    }

    /**
     * Método para pasar a la ventana ver pedidos en estado preparar o recoger
     */
    private void verPedidos() {
        Intent verPedidosIntent = new Intent(PantallaPrincipalActivity.this, VerPedidoActivity.class);
        startActivity(verPedidosIntent);

    }

    /**
     * Método para pasar a la ventana hacer pedido
     */
    private void hacerPedido() {
        Intent hacerPedidoIntent = new Intent(PantallaPrincipalActivity.this, SeleccionarRacionesActivity.class);
        startActivity(hacerPedidoIntent);

    }

    /**
     * Meétodo para ir a la ventana de editar registro
     */
    private void editarRegistro() {
        //Pasar a la vista de editar registro
        Intent editarRegistroIntent   = new Intent(PantallaPrincipalActivity.this, EditarRegistroActivity.class);
        startActivity(editarRegistroIntent);

    }

    /**
     * Recuperar el token y asignarlo al usuario
     */
    private void tokenAppp (){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("ErrorTokenNotificacion", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        // referencia de la base de datos
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        if (usuarioLogeado != null){
                            String userLog = usuarioLogeado.getUid();
                            DatabaseReference usuariosRef = databaseReference.child("usuarios").child(userLog);
                            // token del usuario a BBDD
                            usuariosRef.child("tokenNoti").setValue(token);
                        }
                    }
                });
    }

}