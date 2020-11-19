package br.com.luisfga.talkingz.app.background;

import android.content.Intent;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.database.entity.user.User;
import br.com.luisfga.talkingz.app.ui.OrchestraAbstractRootActivity;
import br.com.luisfga.talkingz.app.ui.TabsActivity;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;

/**
 * Atividade principal.
 *
 * Mostra uma SplashScreen com uma animação de Loading e mensagem de retorno para o usuário
 */
public class SplashMainActivity extends OrchestraAbstractRootActivity {

    OrchestraApp orchestraApp;

    //View da mensagem do que está sendo feito (Carregando..., etc)
    private TextView splashMsg;

    //View das mensagens de retorno (Sucesso, etc)
    private TextView splashResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_splash);

        splashMsg = findViewById(R.id.splash_msg);
        splashResponse = findViewById(R.id.splash_response);

        orchestraApp = (OrchestraApp) getApplication();

        carregarUsuario(); //primeiro passo do carregamento do App.
    }

    /* -----------------------------------------------*/
    /* ------------- LOADING PROCEDURES --------------*/
    /* -----------------------------------------------*/

    /**
     * Primeiro passo da carga do aplicativo.
     * Executado após a tela de Splash ser apresentada.
     * Inicia processamento em segundo plano de toda a pilha de carregamento inicial.
     */
    private void carregarUsuario() {
        AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().execute(() -> {

            User mainUser = orchestraApp.getOrchestraDB().userDAO().getMainUser();

            if (mainUser != null) {
                orchestraApp.setMainUser(mainUser);
            } else {
                User newUser = new User();
                newUser.setMainUser(true);
                newUser.setId(UUID.randomUUID());
                newUser.setName("");
                newUser.setEmail("");
                newUser.setSearchToken("");
                newUser.setJoinTime(System.currentTimeMillis());

                orchestraApp.setMainUser(newUser);
                orchestraApp.getOrchestraDB().userDAO().insert(newUser);

                runOnUiThread(() -> Toast.makeText(orchestraApp, "Novo usuário criado: " + newUser.getId().toString(), Toast.LENGTH_LONG).show());

            }

            inicializarOrchestra();

        });
    }

    private void inicializarOrchestra() {
        runOnUiThread(() -> splashMsg.setText("Abrindo..."));
        //TODO colocar um efeito sonoro indicando que carregou tudo legal
        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.START_VIDEO_RECORDING);

        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

        Intent tabsActivity = new Intent(getApplicationContext(), TabsActivity.class);
        startActivity(tabsActivity);

        if(!orchestraApp.isConnectionOpen())
            orchestraApp.conectar();

        finish();
    }

}