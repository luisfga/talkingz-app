package br.com.luisfga.talkingz.app.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;

public class NetworkStateChangeBroadcastReceiver extends BroadcastReceiver {

    //tag usada com o logger
    private final String TAG = "NetworkChangeBReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.println(Log.INFO, TAG, "Recepção de broadcast");

        Intent coreServiceIntent = new Intent(context, TalkingzService.class);
        intent.setAction(TalkingzService.NETWORK_STATE_CHANGED_START);
        if (isInternetAvailable()) {
            Log.println(Log.INFO, TAG, "Conexão com a internet restabelecida - Iniciando OrchestraCoreService");
            context.startService(coreServiceIntent);
        } else {
            //precisa parar o serviço? acho que não
//            Log.println(Log.INFO, TAG, "Conexão com a internet interrompida - Parando OrchestraCoreService");
//            context.stopService(coreServiceIntent);
        }
    }

    public boolean isInternetAvailable() {
        Future<Boolean> result = AppDefaultExecutor.getOrchestraNormalPriorityThread().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    InetAddress ipAddr = InetAddress.getByName("google.com");
                    //You can replace it with your name
                    return !ipAddr.equals("");
                } catch (Exception e) { return false; }
            }
        });

        try { return result.get(); } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    private void startOrchestraCoreService(Context context, String action) {
        Intent broadcastServiceStart = new Intent(context, TalkingzService.class);
        broadcastServiceStart.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(broadcastServiceStart);
        } else {
            context.startService(broadcastServiceStart);
        }
    }
}