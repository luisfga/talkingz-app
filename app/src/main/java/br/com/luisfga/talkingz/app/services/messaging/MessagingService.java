package br.com.luisfga.talkingz.app.services.messaging;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.database.TalkingzClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.entity.User;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.app.utils.Notification;

import java.util.Timer;
import java.util.TimerTask;

public class MessagingService extends android.app.Service {

    //Timer configuration
    private final long PERIOD = 20000;

    private static final String TAG = MessagingService.class.getSimpleName();

    protected static final int NOTIFICATION_ID = 1337;
    private static MessagingService mCurrentMessagingService;

    private User mainUser;

    public MessagingService() {
        super();
    }

    /**-----------------------------------
     ------- WEBSOCKET CLIENT ACCESS -----
     -----------------------------------*/
    public boolean isConnectionOpen() {
        return MessagingWSClient.getInstance(getApplication()).isConnectionOpen();
    }

    public void clearWebSocket(){
        MessagingWSClient.getInstance(getApplication()).clear();
    }

    public MessagingWSClient getWsClient(){
        return MessagingWSClient.getInstance(getApplication());
    }

    private void connect() {
        MessagingWSClient wsClient = MessagingWSClient.getInstance(getApplication());
        if (mainUser != null && !wsClient.isConnectionOpen()) {
            wsClient.conectar(mainUser.getId().toString());
        }
    }
    /**--------------------------------
    ----------- SERVICE API -----------
    ---------------------------------*/
    //Binder to be used by Components' ServiceConnections
    private final IBinder binder = new Binder();
    public static class Binder extends android.os.Binder {
        public MessagingService getService(){
            return mCurrentMessagingService;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().execute(() -> {
            this.mainUser = TalkingzClientRoomDatabase.getDatabase(this).userDAO().getMainUser();

            NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    Log.d("ConnectivityManager", "Connection available. Connecting to server!");
                    connect();
                }
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    Log.d("ConnectivityManager", "Connection UNavailable. Clearing data!");
                    clearWebSocket();
                }
            });
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartForeground();
        }
        mCurrentMessagingService = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "restarting Service !!");

        // it has been killed by Android and now it is restarted. We must make sure to have reinitialised everything
        if (intent == null) {
            MessagingProcessMainClass bck = new MessagingProcessMainClass();
            bck.launchService(this, MessagingService.class);
        }

        // make sure you call the startForeground on onStartCommand because otherwise
        // when we hide the notification on onScreen it will nto restart in Android 6 and 7
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            restartForeground();
        }

        startTimer();

        // return start sticky so if it is killed by android, it will be restarted with Intent null
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
        // restart the never ending service
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
//        stoptimertask();
    }


    /**
     * it starts the process in foreground. Normally this is done when screen goes off
     * THIS IS REQUIRED IN ANDROID 8 :
     * "The system allows apps to call Context.startForegroundService()
     * even while the app is in the background.
     * However, the app must call that service's startForeground() method within five seconds
     * after the service is created."
     */
    public void restartForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "restarting foreground");
            try {
                Notification notification = new Notification();
                startForeground(NOTIFICATION_ID, notification.setNotification(this, "Service notification", "This is the service's notification", R.drawable.ic_sleep));
                Log.i(TAG, "restarting foreground successful");
                startTimer();
            } catch (Exception e) {
                Log.e(TAG, "Error in notification " + e.getMessage());
            }
        }
    }

    /**
     * this is called when the process is killed by Android
     *
     * @param rootIntent - the root intent
     */

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved called");
        // restart the never ending service
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        // do not call stoptimertask because on some phones it is called asynchronously
        // after you swipe out the app and therefore sometimes
        // it will stop the timer after it was restarted
        // stoptimertask();
    }

    /**
     * static to avoid multiple timers to be created when the service is called several times
     */
    private static Timer timer;
    private static TimerTask timerTask;
    private int counter = 0;

    public void startTimer() {
        Log.i(TAG, "Starting timer");
        counter = 0;
        //set a new Timer - if one is already running, cancel it to avoid two running at the same time
        stoptimertask();//if there is one
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        Log.i(TAG, "Scheduling...");

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, PERIOD, PERIOD); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        Log.i(TAG, "initialising TimerTask");
        timerTask = new TimerTask() {
            public void run() {
                if (isConnectionOpen()) {
                    Log.d(TAG, "Service status check n°"+ (counter++) +": Running beautifully.");
                    MessagingWSClient.getInstance(getApplication()).sendKeepAlivePing();
                } else {
                    Log.d(TAG, "Service status check n°"+ (counter++) +"): Running, but offline. Trying to reconnect...");
                    connect();
                }
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
