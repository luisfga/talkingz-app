package br.com.luisfga.talkingz.app.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ConnectionPoliceBroadcastReceiver extends BroadcastReceiver {

    //tag usada com o logger
    private final String TAG = "ConnectionPolice";

    //identificador para saber de onde vem a invocação
    public static final String ACTION_EXECUCAO_AGENDADA = "br.com.orchestraclient.action.EXECUCAO_AGENDADA";

     //Intervalo do agendador em milisegundos, 60000 = 1 minuto
    public static final long INTERVALO = 60000; //

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.println(Log.INFO, TAG, "Verificação de conexão");

        if (ACTION_EXECUCAO_AGENDADA.equals(intent.getAction())) {
            Log.println(Log.INFO, TAG, "Executando ação agendada - Verificação de conexão");
            startOrchestraCoreService(context, OrchestraCoreService.SCHEDULE_START);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.println(Log.INFO, TAG, "Executando após o BOOT - Iniciando OrchestraCoreService");
            startOrchestraCoreService(context, OrchestraCoreService.BOOT_START);
        }
    }

    private void startOrchestraCoreService(Context context, String action) {
        Intent broadcastServiceStart = new Intent(context, OrchestraCoreService.class);
        broadcastServiceStart.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(broadcastServiceStart);
        } else {
            context.startService(broadcastServiceStart);
        }
    }
}