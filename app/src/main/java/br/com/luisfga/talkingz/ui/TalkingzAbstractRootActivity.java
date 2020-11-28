package br.com.luisfga.talkingz.ui;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import br.com.luisfga.talkingz.TalkingzApp;

@SuppressLint("Registered")
public abstract class TalkingzAbstractRootActivity extends AppCompatActivity {

    protected TalkingzApp getTalkinzApp(){
        return (TalkingzApp) getApplication();
    }
}
