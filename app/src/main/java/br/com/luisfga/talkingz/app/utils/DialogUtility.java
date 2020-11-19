package br.com.luisfga.talkingz.app.utils;

import android.app.AlertDialog;
import android.content.Context;

import br.com.luisfga.talkingz.app.R;

public class DialogUtility {

    public static void showInfoDialog(Context contex, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(contex, R.style.AlertDialogTheme);
        builder.setTitle(title)
                .setCancelable(true)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public static void showConnectionNotAvailableInfo(Context context){
        showInfoDialog(context,"Orchestra", "Conexão indisponível");
    }

}
