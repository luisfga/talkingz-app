/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package br.com.luisfga.talkingz.services.messaging;
/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

/*
 * Created by Fabio Ciravegna, The University of Sheffield. All rights reserved.
 * no part of this code can be used without explicit permission by the author
 * f.ciravegna@shef.ac.uk
 */

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class MessagingProcessMainClass {
    public static final String TAG = MessagingProcessMainClass.class.getSimpleName();
    private static Intent serviceIntent = null;

    /**
     * launching the service
     */
    public void launchService(Context context, Class<?> serviceClass) {
        if (context == null) {
            return;
        }
        setServiceIntent(context, serviceClass);
        // depending on the version of Android we eitehr launch the simple service (version<O)
        // or we start a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        Log.d(TAG, "start service !!!!");
    }

    private void setServiceIntent(Context context, Class<?> serviceClass) {
        if (serviceIntent == null) {
            serviceIntent = new Intent(context,  serviceClass);
        }
    }
}

