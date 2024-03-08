package vidal.juan.cocinapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

/**
 * Modificar el registro del usuario; una vez este ya esté logeado
 */
public class EditarRegistroActivity extends AppCompatActivity {
    //Items de la vista
    private EditText modNombreEditText,modApellidoEditText,modDepartamentoEditText,modTelefonoEditText;
    private Button cancelRegistroButton,modRegistroButton;


    //Uusario logeado en la app
    FirebaseUser usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();
    //Para conectar a la BBDD mediante una referencia
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cocinaapp-7da53-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference myRef = database.getReference().child("usuarios");
    //Consulta para conseguir los datos del usuario logedao;filtrar por el mail
    Query datosUsuarioLogeado = myRef.orderByChild("email").equalTo(usuarioLogeado.getEmail());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_registro);
        //Ref a los campos de la vista
        modNombreEditText = findViewById(R.id.modNombreEditText);
        modApellidoEditText = findViewById(R.id.modApellidoEditText);
        modDepartamentoEditText = findViewById(R.id.modDepartamentoEditText);
        modTelefonoEditText = findViewById(R.id.modTelefonoEditText);
        cancelRegistroButton = findViewById(R.id.cancelRegistroButton);
        modRegistroButton = findViewById(R.id.modRegistroButton);



        //"Ejecutar la consulta"
        obtenerDatosUserLogeado1vez(datosUsuarioLogeado);



        //Acción del boton cancelar
        cancelRegistroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                volverPprincipal();
            }
        });
        //Acción del boton editar registro
        modRegistroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validar los campos modificados para que sea coherente escribirlos en la BBDD
                //TODO borrar prueba Toast.makeText(EditarRegistroActivity.this,"campo mofiicado: " +modNombreEditText.getText().toString()  , Toast.LENGTH_LONG).show();
                validarModRegistro();


            }
        });

    }

    /**
     * Para volver a la ventana principal
     */

    private  void volverPprincipal() {

        finish();
    }

    public void validarModRegistro() {
        String nombre = "";
        String apels = "";
        String departamento = "";
        String telefono = "";
        boolean verfificarEstadoNombre = false;
        boolean verfificarEstadoApel = false;
        boolean verfificarEstadoDepart = false;
        boolean verfificarEstadoTelefono = false;
        //TODO Validar que el nombre introducido no sea mayor a x y que sean caracteres de texto

         nombre = modNombreEditText.getText().toString();
        if (nombre.isEmpty() || !Pattern.compile("^[A-Za-z]+$").matcher(nombre).find())//Todo pone rlimite de caracteres
        {
            modNombreEditText.setError("Nombre invalido");
            verfificarEstadoNombre = false;

        }else {//quitar el error
            modNombreEditText.setError(null);
            verfificarEstadoNombre = true;

         //TODO Validar que apellidos no sea mayor a x y que sean caracteres de texto
        apels = modApellidoEditText.getText().toString();
        }
        if (apels.isEmpty() || !Pattern.compile("^[A-Za-z]+ [A-Za-z]+$").matcher(apels).find())//Todo pone rlimite de caracteres
        {
                modApellidoEditText.setError("Apellidos invalidos");
            verfificarEstadoApel = false;

        }else {//quitar el error
            modApellidoEditText.setError(null);
            verfificarEstadoApel = true;
        }

        //TODO Validar que el departamento introducido no sea mayor a x y que sean caracteres de texto
        departamento = modDepartamentoEditText.getText().toString();
        if (departamento.isEmpty() || !Pattern.compile("^[A-Za-z]+$").matcher(departamento).find())//Todo pone rlimite de caracteres
         {
             modDepartamentoEditText.setError("Departamento invalido");
             verfificarEstadoDepart = false;

         }else {//quitar el error
            modDepartamentoEditText.setError(null);
            verfificarEstadoDepart = true;
        }

        //TODO Validar que el telefono introducido no sea mayor a x y que sean caracteres de texto
        telefono = modTelefonoEditText.getText().toString();
        if (telefono.isEmpty() || !Pattern.compile("^[0-9]{9}$").matcher(telefono).find())
        {
            modTelefonoEditText.setError("Teléfono invalido");
            verfificarEstadoTelefono = false;
        }else
        {//quitar el error
            modTelefonoEditText.setError(null);
            verfificarEstadoTelefono = true;
        }
        if(verfificarEstadoNombre & verfificarEstadoApel & verfificarEstadoDepart & verfificarEstadoTelefono)
        {
            //todo meter ventana de confirmación?¿
            //obtener idusuario autenticado que coincide con la tabla usuarios
            String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //Crear objeto del modelo usuario para actulizarlo en la tabla usuarios
            Usuarios usuarioUpdate = new Usuarios(nombre,apels,usuarioLogeado.getEmail(),departamento,telefono);
            //Actualizar el registro;listener para saber si ha ido bien
            myRef.child(key).setValue(usuarioUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // DATO MODIFICADO EN BBDD
                Toast.makeText(EditarRegistroActivity.this,"Registro modificado "  , Toast.LENGTH_LONG).show();
                //volver a la ventana principal
                volverPprincipal();
            }
            })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditarRegistroActivity.this,"ERROR no se registraron los cambios "  , Toast.LENGTH_LONG).show();
                    }
                });
        }


    }
    //TODO borrar este metodo?¿ es necesario obtener el registro en real time? por si se modifica de la web? en tal caso modificar el otro el listener
    /**
     * Método para obtener los datos personales del usuario logeado
     * @param datosUsuarioLogeado Query con referencia al email del usuario logeado
     */
    public void obtenerDatosUserLogeado(Query datosUsuarioLogeado) {
        datosUsuarioLogeado.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //El resultado se devuelve en un DataSnapshot, hay que recorrer con un FOR aunq solo sea un resultado
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren())
                {
                    //Crear un objeto usuario a partir de los datos filtrados para el correo del usuario logeado
                    Usuarios usuarioDatosAux = userSnapshot.getValue(Usuarios.class);
                    //Obtener los datos para mostrarlos en la vista
                    String nombreUserOriginal = usuarioDatosAux.getNombre();
                    String apellidosOriginal = usuarioDatosAux.getApellidos();
                    String departamentoOriginal = usuarioDatosAux.getDepartamento();
                    String telefonoOriginal = usuarioDatosAux.getTelefono();
                    //Pasar los datos origianles a la vista
                    modNombreEditText.setText(nombreUserOriginal);
                    modApellidoEditText.setText(apellidosOriginal);
                    modDepartamentoEditText.setText(departamentoOriginal);
                    modTelefonoEditText.setText(telefonoOriginal);
                    verificarModificacionCampo(modNombreEditText, nombreUserOriginal);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // fallo al obtener los datos
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        //Toast.makeText(EditarRegistroActivity.this,usuarioDatosOriginal.getNombre(), Toast.LENGTH_LONG).show();

    }

    /***
     * Metodo para leer slo una vez los datos del usuario logeado, no se modifica aunq se modifique en la BBDD
     * @param datosUsuarioLogeado
     */
    public void obtenerDatosUserLogeado1vez(Query datosUsuarioLogeado) {

        datosUsuarioLogeado.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nombreUserOriginal ="usuario_original";
                String apellidosOriginal = "Apel_origianl";
                String departamentoOriginal = "dep_origianl";
                String telefonoOriginal = "tel_original";
                //El resultado se devuelve en un DataSnapshot, hay que recorrer con un FOR aunq solo sea un resultado
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren())
                {
                    //Crear un objeto usuario a partir de los datos filtrados para el correo del usuario logeado
                    Usuarios usuarioDatosAux = userSnapshot.getValue(Usuarios.class);
                    if(usuarioDatosAux != null) {
                        //Obtener los datos para mostrarlos en la vista
                        nombreUserOriginal = usuarioDatosAux.getNombre();
                        apellidosOriginal = usuarioDatosAux.getApellidos();
                        departamentoOriginal = usuarioDatosAux.getDepartamento();
                        telefonoOriginal = usuarioDatosAux.getTelefono();
                        //Pasar los datos origianles a la vista
                        modNombreEditText.setText(nombreUserOriginal);
                        modApellidoEditText.setText(apellidosOriginal);
                        modDepartamentoEditText.setText(departamentoOriginal);
                        modTelefonoEditText.setText(telefonoOriginal);
                    }
                }
                //Funcionalidad de EditText en caso de que los campos sean modiifcados
                alCambiarTextoCampo(modNombreEditText, nombreUserOriginal );
                alCambiarTextoCampo(modApellidoEditText, apellidosOriginal);
                alCambiarTextoCampo(modDepartamentoEditText, departamentoOriginal);
                alCambiarTextoCampo(modTelefonoEditText, telefonoOriginal);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // fallo al obtener los datos
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });


    }

    /**
     * Método para notificar visualmente al usuario de una modificación;se apoya en la clase TexChangedListener
     * @param campoEvaluar EditText a evaluar
     * @param campoOriginal string orginal obtenido de la bbdd
     */
    private void alCambiarTextoCampo(EditText campoEvaluar, String campoOriginal) {
        campoEvaluar.addTextChangedListener(new TextChangedListener<EditText>(campoEvaluar) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                //Lo qeu hace el campo al ser modificado;
                //campoEvaluar.setError("Nombre modificado");

                //Si el campo no es igual que el obtenido en origen en la BBDD
                if (!campoEvaluar.getText().toString().equals(campoOriginal))
                {
                    //TODO borrar esta prueba en cod final Toast.makeText(EditarRegistroActivity.this,"campo mofiicado: " +campoEvaluar.getText().toString() + "Capor original: " + campoOriginal , Toast.LENGTH_LONG).show();
                    campoEvaluar.setError("Dato modificado");//Informar de que el campo ha sido modificado
                }
                else campoEvaluar.setError(null);//En caso de que sean iguales-->Quitar la notificación
            }
        });
    }


    public void verificarModificacionCampo(EditText campoValidar, String campoOriginal)
    {
        //Toast.makeText(EditarRegistroActivity.this,"camp original: " + campoOriginal, Toast.LENGTH_LONG).show();
        if (!campoValidar.getText().toString().equals(campoOriginal))
        {
            campoValidar.setError("Dato modificado");
        }
        else campoValidar.setError(null);
    }

}
