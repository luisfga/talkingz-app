package br.com.luisfga.talkingz.ui;

import android.media.MediaActionSound;
import android.os.Bundle;

import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.TalkingzApp;
import br.com.luisfga.talkingz.utils.AppDefaultExecutor;

/**
 * Shows a splash screen with loading animation
 * @author luisfga
 */
public class SplashScreenActivity extends TalkingzAbstractRootActivity {

    TalkingzApp talkingzApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        talkingzApp = (TalkingzApp) getApplication();

        AppDefaultExecutor.getTalkingzBackloadMaxPriorityThread().execute(() -> {

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