package br.com.luisfga.talkingz.app.ui;

import android.media.MediaActionSound;
import android.os.Bundle;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.background.TalkinzApp;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;

/**
 * Shows a splash screen with loading animation
 * @author luisfga
 */
public class SplashScreenActivity extends OrchestraAbstractRootActivity {

    TalkinzApp talkinzApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_splash);

        talkinzApp = (TalkinzApp) getApplication();

        AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().execute(() -> {

            //sleep a while (loading... dramatic pause)
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

            //play sound
            MediaActionSound sound = new MediaActionSound();
            //TODO set another fancy sound
            sound.play(MediaActionSound.START_VIDEO_RECORDING);

            //sleep a while (loading... dramatic pause)
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

            finish();

        });

    }
}