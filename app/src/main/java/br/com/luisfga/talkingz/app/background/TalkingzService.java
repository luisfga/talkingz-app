package br.com.luisfga.talkingz.app.background;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;

public class TalkingzService extends Service {

    public static final String NETWORK_STATE_CHANGED_START = "NETWORK_STATE_CHANGED_START";

    private final String TAG = "TalkingzService";
    private TalkingzBinder binder;

    private TalkingzApp talkingzApp;

    public TalkingzService() {
        super();
        binder = new TalkingzBinder();
    }

    /* -----------------------------------------------*/
    /* ---------- INÍCIO - SERVICE API ---------------*/
    /* -----------------------------------------------*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.println(Log.DEBUG, TAG, "onCreate");

        talkingzApp = (TalkingzApp) getApplication();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.println(Log.DEBUG, TAG, "onStartCommand - startId:"+startId);

//        if (intent.getAction().equals(SCHEDULED_KEEPALIVE_CHECK)) {
//            Log.println(Log.DEBUG, TAG, "Checking connection");
//            if (talkingzApp.isConnectionOpen()) {
//                Log.println(Log.DEBUG, TAG, "Connection was already open. Sending ping message!");
//                talkingzApp.getWsClient().sendPingMessage();
//            } else {
//                Log.println(Log.DEBUG, TAG, "Connection was closed");
//                talkingzApp.connect();
//            }
//
//        }

        return START_STICKY;
    }

    public class TalkingzBinder extends Binder {
        public TalkingzService getService() {
            return TalkingzService.this;
        }
    }
}