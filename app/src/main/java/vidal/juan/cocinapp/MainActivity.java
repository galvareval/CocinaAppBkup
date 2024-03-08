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
        //TODO hacer splash screen??¿?¿?
        //Si el usuario ya esta logeado, pasar a la pantalla princiapl; TODO no se si funciona sin el splash
        //Obtener el usuario logeado
        FirebaseUser usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioLogeado != null){
            Intent usuarioYaLogeadoInt = new Intent(MainActivity.this, PantallaPrincipalActivity.class);
            startActivity(usuarioYaLogeadoInt);
            finish();
        }

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
                Intent intent = new Intent(MainActivity.this, PantallaPrincipalActivity.class);
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
        mAuth = FirebaseAuth.getInstance();
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
     * Validar que el usuario introducido sea un correo electrónico y que la conraseña no este vacia
     */
    public void validar()
    {
        String correo = usuarioEditText.getText().toString().trim();
        String psw = contrasenaEditText.getText().toString().trim();
        //Comprobar el correo
        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches())
        {
            usuarioEditText.setError("Dirección de correo invalida");
            return;
        }else
        {//quitar el error
            usuarioEditText.setError(null);
        }
        //Comprobar la contraseña
        if (psw.isEmpty() )  {//TODO validar tambien el patron de la psw definir el patron de la psw

            contrasenaEditText.setError("Introduce la contraseña");
            return;
        }
        else
        {
            contrasenaEditText.setError(null);
        }
        iniciarSesion(correo,psw);

    }

    /**
     * Metodo para iniciar sesión una vez validados los campos de email y psw
     * @param correo Correo del usuario que intenta inciar sesión
     * @param psw Contraseña del usuario que intenta inciar sesión
     */
    public void iniciarSesion(String correo, String psw)
    {
        //Autentificiación mediante firebase con el email y psw proporciado
        mAuth.signInWithEmailAndPassword(correo,psw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //Si la autentificacion es correcta pasar a la pantalla principal
                if (task.isSuccessful()){
                    Intent logOkInt = new Intent(MainActivity.this, PantallaPrincipalActivity.class);
                    startActivity(logOkInt);
                    finish();
                }
                //En caso de autentificación erronea, notificarlo
                else {
                    Toast.makeText(MainActivity.this,"Autentificación incorrecta", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
