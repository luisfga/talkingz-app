package br.com.luisfga.talkingz.app.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class InternetUtils {

    public static boolean isInternetAvailable() {
        InetAddress ipAddr = null;
        try {
            ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.isReachable(5000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //You can replace it with your name
        return false;
    }
}
