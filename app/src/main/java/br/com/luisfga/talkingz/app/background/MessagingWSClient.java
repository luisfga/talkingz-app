package br.com.luisfga.talkingz.app.background;

import android.content.Context;
import android.util.Log;

import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.websocket.*;

import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.commons.orchestration.Orchestration;
import br.com.luisfga.talkingz.commons.orchestration.OrchestrationDecoder;
import br.com.luisfga.talkingz.commons.orchestration.OrchestrationEncoder;

@ClientEndpoint(
        decoders = {OrchestrationDecoder.class},
        encoders = {OrchestrationEncoder.class})
public class MessagingWSClient {

//    private final String SERVER_ENDPOINT_URI = "wss://192.168.0.7:8443/MessagingWSEndpoint";
    private final String SERVER_ENDPOINT_URI = "ws://talkingz.herokuapp.com/MessagingWSEndpoint";
//    private final String SERVER_ENDPOINT_URI = "ws://192.168.0.7:8080/MessagingWSEndpoint";
    private final long DELAY_FOR_PING = 45;

    private final String TAG = "MessagingWSClient";
    private Session userSession = null;

    private static OrchestraMessageHandler messageHandler;
    private static MessagingWSClient INSTANCE;

    private Context applicationContext;

    static MessagingWSClient getIntansce(OrchestraMessageHandler handler){
        messageHandler = handler;
        if (INSTANCE == null || INSTANCE.getUserSession() == null || !INSTANCE.getUserSession().isOpen()){
            INSTANCE = new MessagingWSClient();
        }
        return INSTANCE;
    }

    static void clear() {
        INSTANCE = null;
    }

    boolean isConnectionOpen() {
        return INSTANCE != null
                && INSTANCE.getUserSession() != null
                && INSTANCE.getUserSession().isOpen();
    }


    /**************
     * CONSTRUCTOR
     **************/
    void conectar(Context applicationContext, String userId) {
        this.applicationContext = applicationContext;

        AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().execute(() -> {
            try {
                URI remoteURI = new URI(SERVER_ENDPOINT_URI+"/"+userId);



                ClientManager clientManager = ClientManager.createClient();
//            clientManager.getProperties().put("org.glassfish.tyrus.incomingBufferSize", 110000000); Transferência de arquivo movida para FileTransferWSClient
                clientManager.setAsyncSendTimeout(-1);

                //SSL Configuration
//            SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(SSLUtility.getConfiguredSSLContext(applicationContext), true, false, false);
//            sslEngineConfigurator.setHostVerificationEnabled(false);
//            clientManager.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);

                clientManager.connectToServer(this, remoteURI);

                Log.println(Log.DEBUG, TAG, "Conectado ao servidor");

                Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                    MessagingWSClient.this.sendPingMessage();
                }, DELAY_FOR_PING, DELAY_FOR_PING, TimeUnit.SECONDS);

            } catch (URISyntaxException uriSyntaxException) {
                messageHandler.onConnectionError("URISyntaxException ao tentar conectar ao servidor");

            } catch (DeploymentException deploymentException) {
                messageHandler.onConnectionError("DeploymentException ao tentar conectar ao servidor: " + deploymentException.getMessage());

            } catch (IOException ioException) {
                messageHandler.onConnectionError("IOException ao tentar conectar ao servidor: " + ioException.getMessage());

//        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException sslException) {
//            messageHandler.onConnectionError("Erro no handshake SSL ao tentar conectar ao servidor: " + sslException.getMessage());

            } catch (Exception genericException) {
                messageHandler.onConnectionError("Exception ao tentar conectar ao servidor. " + genericException.getMessage());
            }
        });

    }

    /**************
     * GETTERs and SETTERs
     **************/
    Session getUserSession() {
        return userSession;
    }

    /**************
     * WEBSOCKET API - implementação de métodos abstratos
     **************/
    @OnOpen
    public void onOpen(Session userSession, EndpointConfig config) {
        this.userSession = userSession;
        this.messageHandler.onConnectionOpen();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        this.messageHandler.onConnectionClose();
        clear();
        Log.println(Log.DEBUG, TAG, "Conexão fechada. Reason: " + reason.toString());
    }

    @OnError
    public void onError(Throwable error, Session session) {
        Log.e(TAG, error.getMessage());
    }

    @OnMessage
    public void onMessage(Orchestration orchestration) {
        this.messageHandler.onMessage(orchestration);
    }

    @OnMessage
    public void onMessage(PongMessage pongMessage) {
        byte[] pongMessageBytes = pongMessage.getApplicationData().array();
        Log.d(TAG, "Pong message received: " + new String(pongMessageBytes));
    }

    public void sendCommandOrFeedBack(Orchestration orchestration) {
        AppDefaultExecutor.getOrchestraLowPriorityNetworkingThreadPool().execute(() -> userSession.getAsyncRemote().sendObject(orchestration));
    }

    public void sendPingMessage(){

        //check here because the caller maybe the Scheduler and in the mean time the connection can be closed.
        if (!isConnectionOpen()) return;

        ByteBuffer pingMsg = ByteBuffer.wrap(new Long(System.currentTimeMillis()).toString().getBytes());
        AppDefaultExecutor.getOrchestraLowPriorityNetworkingThreadPool().execute(() -> {
            try {
                Log.d(TAG, "Sending ping");
                userSession.getAsyncRemote().sendPing(pingMsg);
            } catch (IOException e) {
                Log.e(TAG,"Error on trying to ping.", e);
            }
        });
    }

    interface OrchestraMessageHandler extends MessageHandler.Whole<Orchestration> {
        void onConnectionOpen();
        void onConnectionClose();
        void onConnectionError(String errorMessage);
    }
}