package br.com.luisfga.talkingz.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import br.com.luisfga.talkingz.app.database.TalkingzClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.entity.User;
import br.com.luisfga.talkingz.app.services.messaging.MessagingService;
import br.com.luisfga.talkingz.app.services.messaging.MessagingWSClient;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;

import java.util.UUID;

/**
 * @author luisfga
 */
public class TalkingzApp extends Application  {

    private final String TAG = TalkingzApp.class.getSimpleName();
    private User mainUser;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().execute(() -> {
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

            Looper.prepare();
            Toast.makeText(this, "Novo usuário criado: " + mainUser.getId().toString(), Toast.LENGTH_LONG).show();
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
