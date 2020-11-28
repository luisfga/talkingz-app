package br.com.luisfga.talkingz.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import br.com.luisfga.talkingz.R;

public class SSLUtility {

    public static SSLContext getConfiguredSSLContext(Context applicationContext) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, KeyManagementException {
        String keyStoreType = "pkcs12";
        String password = "orchestra";

        KeyStore keyStore = KeyStore.getInstance(keyStoreType);

        //get inputStream from .keystore file
//        InputStream keyStoreData = applicationContext.getResources().openRawResource(R.raw.orchestra_pkcs12_keystore);

        //loads it into the keystore instance (data and password)
//        keyStore.load(keyStoreData, password.toCharArray());

        // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }

}
