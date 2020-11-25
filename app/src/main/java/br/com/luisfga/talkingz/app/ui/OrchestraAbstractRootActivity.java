package br.com.luisfga.talkingz.app.ui;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import br.com.luisfga.talkingz.app.TalkingzApp;

@SuppressLint("Registered")
public abstract class OrchestraAbstractRootActivity extends AppCompatActivity {

    protected TalkingzApp getTalkinzApp(){
        return (TalkingzApp) getApplication();
    }
}
