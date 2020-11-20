package br.com.luisfga.talkingz.app.background;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class TalkingzService extends Service {

    public static final String APP_START = "APP_START";
    public static final String SCHEDULE_START = "SCHEDULE_START";
    public static final String BOOT_START = "BOOT_START";
    public static final String NETWORK_STATE_CHANGED_START = "NETWORK_STATE_CHANGED_START";

    private final String TAG = "ConnectivityService";
    private TalkingzBinder binder;

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

        Log.println(Log.INFO, TAG, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.println(Log.INFO, TAG, "onStartCommand - startId:"+startId);

        return START_STICKY;
    }

    public class TalkingzBinder extends Binder {
        public TalkingzService getService() {
            return TalkingzService.this;
        }
    }
}