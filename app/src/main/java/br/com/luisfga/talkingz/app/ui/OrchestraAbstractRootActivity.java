package br.com.luisfga.talkingz.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.com.luisfga.talkingz.app.background.TalkinzApp;

@SuppressLint("Registered")
public abstract class OrchestraAbstractRootActivity extends AppCompatActivity {

    protected TalkinzApp getTalkinzApp(){
        return (TalkinzApp) getApplication();
    }
}
