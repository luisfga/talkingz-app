package br.com.luisfga.talkingz;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import br.com.luisfga.talkingz.services.messaging.MessagingProcessMainClass;
import br.com.luisfga.talkingz.services.messaging.MessagingService;
import br.com.luisfga.talkingz.services.messaging.MessagingServiceRestarterBroadcastReceiver;
import br.com.luisfga.talkingz.ui.TalkingzAbstractRootActivity;
import br.com.luisfga.talkingz.ui.SplashScreenActivity;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends TalkingzAbstractRootActivity {

    private final String TAG = "MainActivity";

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // remove title (to not show anything before splash screen
        // line commented and set on Manifest by theme
//        this.setFullscreen(true);

        //show timed splash screen
        Intent splashScreenIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
        startActivity(splashScreenIntent);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //set user name on header
        TextView navHeaderUserName =  navigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        navHeaderUserName.setText(getTalkingzApp().getMainUser().getName());
        //set user search token on header
        TextView navHeaderSearchToken =  navigationView.getHeaderView(0).findViewById(R.id.nav_header_search_token);
        navHeaderSearchToken.setText(getTalkingzApp().getMainUser().getSearchToken());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setFullscreen(false);
    }

    private void setFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTalkingzApp().clearNotifications();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            MessagingServiceRestarterBroadcastReceiver.scheduleJob(getApplicationContext());
        } else {
            MessagingProcessMainClass bck = new MessagingProcessMainClass();
            bck.launchService(getApplicationContext(), MessagingService.class);
        }
        //forçando esconder o teclado, pois estava aparecendo ao voltar da tela de conversa
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}