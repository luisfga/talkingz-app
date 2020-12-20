package br.com.luisfga.talkingz;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import br.com.luisfga.talkingz.database.TalkingzClientRoomDatabase;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.services.messaging.MessagingService;
import br.com.luisfga.talkingz.utils.AppDefaultExecutor;

import java.util.UUID;

/**
 * @author luisfga
 */
public class TalkingzApp extends Application  {

    private final String TAG = TalkingzApp.class.getSimpleName();
    private User mainUser;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        AppDefaultExecutor.getTalkingzBackloadMaxPriorityThread().execute(() -> {
            Log.d(TAG, "About to load user");
            loadUser(); //this loading here is crucial
            Intent intent = new Intent(this, MessagingService.class);
            Log.d(TAG, "Starting MessagingService from TalkingzApp.onCreate()");
            startService(intent);
            Log.d(TAG, "Binding to the service from TalkingzApp.onCreate()");
            bindService(intent, serviceBoundConnection, Context.BIND_IMPORTANT);
        });
    }

    //USUÁRIO
    public User getMainUser() {
        return mainUser;
    }

    //BANCO DE DADOS
    public TalkingzClientRoomDatabase getTalkingzDB() {
        return TalkingzClientRoomDatabase.getDatabase(this);
    }

    public boolean isConnectionOpen(){
        return this.messagingService != null && this.messagingService.isConnectionOpen();
    }

    public void showToast(String message, int length){
        mHandler.post(() -> {

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_toast, null);

            TextView text = (TextView) layout.findViewById(R.id.toast_text);
            text.setText(message);

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 150);
            toast.setDuration(length);
            toast.setView(layout);
            toast.show();
        });
    }

    /* -----------------------------------------------*/
    /* ----------- CONFIG AND INITIALIZATION ---------*/
    /* -----------------------------------------------*/
    private void loadUser() {
        mainUser = getTalkingzDB().userDAO().getMainUser();
        if (mainUser == null) {
            mainUser = new User();
            mainUser.setMainUser(true);
            mainUser.setId(UUID.randomUUID());
            mainUser.setName("");
            mainUser.setEmail("");
            mainUser.setSearchToken("");
            mainUser.setJoinTime(System.currentTimeMillis());

            getTalkingzDB().userDAO().insert(mainUser);

            showToast("Novo usuário criado: " + mainUser.getId().toString(), Toast.LENGTH_LONG);

        }
        Log.d(TAG, "loadUser(): User loaded? " + (mainUser != null));
    }

    /* -----------------------------------------------*/
    /* --------------- SERVICE BINDING ---------------*/
    /* -----------------------------------------------*/
    MessagingService messagingService;
    boolean isBound = false;
    private final ServiceConnection serviceBoundConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessagingService.Binder messageServicebinder = (MessagingService.Binder) service;
            messagingService = messageServicebinder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    public MessagingService getMessagingService() {
        return messagingService;
    }
}
