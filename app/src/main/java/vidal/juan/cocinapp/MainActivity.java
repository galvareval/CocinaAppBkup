package vidal.juan.cocinapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    //Para hacer la autentificación
    private FirebaseAuth mAuth;
    //Items del xml
    Button loginButton;
    EditText usuarioEditText,contrasenaEditText;
    TextView olvidoContrasenaTextView, registroTextView;

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
