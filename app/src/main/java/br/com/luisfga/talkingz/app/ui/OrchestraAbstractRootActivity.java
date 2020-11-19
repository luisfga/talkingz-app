package br.com.luisfga.talkingz.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.com.luisfga.talkingz.app.background.OrchestraApp;

@SuppressLint("Registered")
public abstract class OrchestraAbstractRootActivity extends AppCompatActivity {

    protected OrchestraApp orchestraApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orchestraApp = (OrchestraApp) getApplication();
    }
}
