package br.com.luisfga.talkingz;

import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import br.com.luisfga.talkingz.database.TalkingzClientRoomDatabase;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.services.messaging.MessagingService;
import br.com.luisfga.talkingz.utils.AppDefaultExecutor;

import java.util.HashSet;
import java.util.Set;
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
            createNotificationChannel();
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

    /* --------------- NOTIFICATIONS HANDLING ---------------- */
    //TODO refazer em classe separada e mais organizada
    private Set<Integer> notificationsIDs = new HashSet<>();
    private Set<Notification> notifications = new HashSet<>();
    private final String NOTIFICATIONS_CHANNEL_ID = "TalkingzNotificationChannelID";
    private final String NOTIFICATIONS_GROUP_KEY = "NOTIFICATIONS_GROUP_KEY";
    private final int SUMMARY_ID = 0;
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.talkingzNotificationChannel);
            String description = getString(R.string.talkingzNotificationDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATIONS_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int generateNewNotificationID(){
        int newID = notificationsIDs.size()+1;
        notificationsIDs.add(newID);
        return newID;
    }

    public void postNewMessageNotification(String title, String text){

        if (notifications.size() > 0) {
            text = "(+"+notifications.size()+") "+ text;
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATIONS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_new_message)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setGroup(NOTIFICATIONS_GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        Notification notfication = builder.build();
        notifications.add(notfication);
        manager.notify(SUMMARY_ID, notfication);
    }

    public void clearNotifications(){
        this.notifications = new HashSet<>();
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
