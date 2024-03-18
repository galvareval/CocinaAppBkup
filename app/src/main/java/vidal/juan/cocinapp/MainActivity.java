package vidal.juan.cocinapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    //Para hacer la autentificación
    private FirebaseAuth mAuth;
    //Items del xml
    Button loginButton;
    EditText usuarioEditText,contrasenaEditText;
    TextView olvidoContrasenaTextView, registroTextView;
    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Si el usuario ya esta logeado, pasar a la pantalla princiapl;
        //Obtener el usuario logeado
        FirebaseUser usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioLogeado != null){
            Intent usuarioYaLogeadoInt = new Intent(MainActivity.this, PantallaPrincipalActivity.class);
            startActivity(usuarioYaLogeadoInt);
            finish();
        }
        //Preguntar por la notificacion, en andorid 13+ es obligatorio
        askNotificationPermission();

        //Obtener instancia de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //Referenciar los items del xml
        loginButton = findViewById(R.id.loginButton);
        registroTextView = findViewById(R.id.registroTextView);
        olvidoContrasenaTextView = findViewById(R.id.olvidoContrasenaTextView);
        usuarioEditText = findViewById(R.id.usuarioEditText);
        contrasenaEditText = findViewById(R.id.contrasenaEditText);

        //REGISTRO
        //Listener del nuevo registro
        registroTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //OLVIDO DE CONTRASEÑA
        //Listener de olvido contraseña
        olvidoContrasenaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReestablecerPswActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //LOGIN
        //Listener del boton login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validar los campos de iniciar sesión
                validar();
            }
        });
    }
    //Pregunta por notificaciones Andorid 13+

    private void askNotificationPermission() {
        // Esto es necesario solo para API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.REQUEST_NOTIFICATIONS") ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (y tu aplicación) pueden enviar notificaciones.
            } else if (shouldShowRequestPermissionRationale("android.permission.REQUEST_NOTIFICATIONS")) {
                // TODO: Mostrar una IU educativa explicando al usuario las características que se habilitarán
                //       mediante la concesión del permiso REQUEST_NOTIFICATIONS. Esta IU debería proporcionar al usuario
                //       botones "Aceptar" y "No gracias". Si el usuario selecciona "Aceptar", solicite directamente el permiso.
                //       Si el usuario selecciona "No gracias", permita que el usuario continúe sin notificaciones.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Esta aplicación necesita el permiso para enviar notificaciones con el fin de proporcionarte actualizaciones importantes y notificaciones relevantes")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Aquí podrías solicitar directamente el permiso
                                requestPermissionLauncher.launch("android.permission.REQUEST_NOTIFICATIONS");
                            }
                        })
                        .setNegativeButton("No gracias", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Aquí podrías permitir que el usuario continúe sin notificaciones
                            }
                        });
                // Mostrar el diálogo
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Solicitar directamente el permiso
                requestPermissionLauncher.launch("android.permission.REQUEST_NOTIFICATIONS");
            }
        }
    }


    /**
     * Validar que el usuario introducido sea un correo electrónico y que la contraseña no esté vacía
     */
    public void validar() {
        String correo = usuarioEditText.getText().toString().trim();
        String psw = contrasenaEditText.getText().toString().trim();

        //Comprobar el correo
        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            usuarioEditText.setError("Dirección de correo inválida");
            return;
        } else {
            //quitar el error
            usuarioEditText.setError(null);
        }

        //Comprobar la contraseña
        if (psw.isEmpty()) {
            contrasenaEditText.setError("Introduce la contraseña");
            return;
        } else {
            contrasenaEditText.setError(null);
        }

        iniciarSesion(correo, psw);
    }

    /**
     * Metodo para iniciar sesión una vez validados los campos de email y contraseña
     * @param correo Correo del usuario que intenta iniciar sesión
     * @param psw Contraseña del usuario que intenta iniciar sesión
     */
    public void iniciarSesion(String correo, String psw) {
        //Autentificación mediante firebase con el email y contraseña proporcionados
        mAuth.signInWithEmailAndPassword(correo, psw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //Si la autentificación es correcta, verificar si el correo ha sido validado
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        //Si el correo ha sido validado, pasar a la pantalla principal
                        Intent logOkInt = new Intent(MainActivity.this, PantallaPrincipalActivity.class);
                        startActivity(logOkInt);
                        finish();
                    } else {
                        //Si el correo no ha sido validado, mostrar un mensaje al usuario
                        Toast.makeText(MainActivity.this, "Por favor, verifique su correo electrónico.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //En caso de autentificación fallida, notificarlo
                    Toast.makeText(MainActivity.this, "Autentificación incorrecta", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
