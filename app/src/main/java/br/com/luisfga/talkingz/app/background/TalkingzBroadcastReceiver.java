package br.com.luisfga.talkingz.app.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TalkingzBroadcastReceiver extends BroadcastReceiver {

    //tag for logger
    private final String TAG = "TalkingzBReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Log.println(Log.DEBUG, TAG, "BootCompleted broadcast received");
//            startOrchestraCoreService(context, TalkingzService.BOOT_START);
//        }
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