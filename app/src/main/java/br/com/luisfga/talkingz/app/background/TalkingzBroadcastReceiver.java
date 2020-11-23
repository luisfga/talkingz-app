package br.com.luisfga.talkingz.app.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class TalkingzBroadcastReceiver extends BroadcastReceiver {

    //tag for logger
    private final String TAG = "TalkingzBReceiver";

    //invocation identifiers
    public static final String ACTION_TALKINGZ_KEEP_ALIVE_PING = "br.com.luisfga.talkingz.action.TALKINGZ_KEEP_ALIVE_PING";

     //Scheduler interval in milli seconds (60000 = 1 minute)
    public static final long INTERVAL = 45000; //

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_TALKINGZ_KEEP_ALIVE_PING.equals(intent.getAction())) {
            Log.println(Log.INFO, TAG, "KeepAlive Ping");
            startOrchestraCoreService(context, TalkingzService.SCHEDULE_START);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.println(Log.INFO, TAG, "BootCompleted broadcast received");
            startOrchestraCoreService(context, TalkingzService.BOOT_START);
        }
    }

    private void startOrchestraCoreService(Context context, String action) {
        Intent broadcastServiceStart = new Intent(context, TalkingzService.class);
        broadcastServiceStart.setAction(action);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(broadcastServiceStart);
//        } else {
            context.startService(broadcastServiceStart);
//        }
    }
}