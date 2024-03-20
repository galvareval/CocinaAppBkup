package vidal.juan.cocinapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ReestablecerPswActivity extends AppCompatActivity {
    //resetear contraseña items
    Button resetPswButton;
    EditText usuarioEditText;
    @Override
    public void onBackPressed() {

        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reestablecer_psw);
        //Set del boton y el texto de la vista
        resetPswButton = findViewById(R.id.resetPswButton);
        usuarioEditText = findViewById(R.id.usuarioEditText);
        //Al pulsar el boton de recuperar
        resetPswButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validar que sea un correo electronico
                validar();
            }
        });

    }

    /**
     * Validar que el correo sea un correo electrónico
     */
    public void validar()
    {
        String correo = usuarioEditText.getText().toString().trim();
        //Si el correo no es valido notificarlo
        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            usuarioEditText.setError("Dirección de correo invalida");
            return;
        }
        //Enviar correo si es valido y esta registrado en la App
        //Borrar: hacer la comprobación de si el usuario existe en la BBDD de usuarios; apar
        enviarCorreoCambioPsw(correo);
    }

    /**
     * Enviar correo firebase auth para elcamio de PSW
     * @param correo
     */
    public void enviarCorreoCambioPsw(String correo){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String correoAux = correo;
        auth.sendPasswordResetEmail(correoAux).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){//Si se envía el correo volver a la pantalla de login
                    Toast.makeText(ReestablecerPswActivity.this, "Correo para resestablecer enviado", Toast.LENGTH_SHORT).show();
                    Intent volverLogin   = new Intent(ReestablecerPswActivity.this, MainActivity.class);
                    startActivity(volverLogin);
                    //finish();
                }else{
                    Toast.makeText(ReestablecerPswActivity.this,"Correo inválido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
