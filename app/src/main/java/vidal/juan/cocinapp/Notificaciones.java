package vidal.juan.cocinapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class Notificaciones extends FirebaseMessagingService {

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    private FirebaseUser usuarioLogeado ;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("tokenGenerado", "token: " + token);
        actuTokenUser(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Obtener  notificación
        String notMsg = message.getNotification().getBody();
        if (notMsg != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), notMsg, Toast.LENGTH_LONG).show());
        }
    }


    private void actuTokenUser(String token) {
        // referencia de la base de datos
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        usuarioLogeado = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioLogeado != null){
            String userLog = usuarioLogeado.getUid();
            DatabaseReference usuariosRef = databaseReference.child("usuarios").child(userLog);
            // Actualizar el token del usuario en la base de datos
            usuariosRef.child("tokenNoti").setValue(token);
        }
    }
}
