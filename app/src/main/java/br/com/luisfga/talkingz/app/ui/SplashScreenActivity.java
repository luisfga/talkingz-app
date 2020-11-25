package br.com.luisfga.talkingz.app.ui;

import android.media.MediaActionSound;
import android.os.Bundle;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.TalkingzApp;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;

/**
 * Shows a splash screen with loading animation
 * @author luisfga
 */
public class SplashScreenActivity extends OrchestraAbstractRootActivity {

    TalkingzApp talkingzApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        talkingzApp = (TalkingzApp) getApplication();

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