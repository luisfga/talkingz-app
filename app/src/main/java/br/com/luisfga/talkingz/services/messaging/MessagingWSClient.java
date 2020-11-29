package br.com.luisfga.talkingz.services.messaging;

import android.app.Application;
import android.util.Log;

import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandGetFile;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseHandler;
import br.com.luisfga.talkingz.services.messaging.handling.TalkingzMessageHandler;
import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.websocket.*;

import br.com.luisfga.talkingz.utils.AppDefaultExecutor;
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

    private final String TAG = MessagingWSClient.class.getSimpleName();
    private Session userSession = null;

    private final TalkingzMessageHandler messageHandler;
    private static MessagingWSClient INSTANCE;

    static MessagingWSClient getInstance(Application application){
        if (INSTANCE == null || INSTANCE.userSession == null || !INSTANCE.userSession.isOpen()){
            INSTANCE = new MessagingWSClient(application);
        }
        return INSTANCE;
    }

    private MessagingWSClient(Application application){
        this.messageHandler = new TalkingzMessageHandler(this, application);
    }

    public void clear() {
        INSTANCE = null;
    }

    boolean isConnectionOpen() {
        return INSTANCE != null
                && INSTANCE.userSession != null
                && INSTANCE.userSession.isOpen();
    }

    public void setResponseCommandFindContactHandler(ResponseHandler<ResponseCommandFindContact> handler){
        this.messageHandler.getTalkingzResponseDispatcher().setResponseCommandFindContactHandler(handler);
    }

    public void setResponseCommandGetFileHandler(ResponseHandler<ResponseCommandGetFile> handler){
        this.messageHandler.getTalkingzResponseDispatcher().setResponseCommandGetFileHandler(handler);
    }

    void conectar(String userId) {

        if(isConnectionOpen()) return;

        AppDefaultExecutor.getTalkingzBackloadMaxPriorityThread().execute(() -> {
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

            } catch (URISyntaxException uriSyntaxException) {
                Log.d(TAG, "onConnectionError: URISyntaxException ao tentar conectar ao servidor");

            } catch (DeploymentException deploymentException) {
                Log.d(TAG, "onConnectionError: DeploymentException ao tentar conectar ao servidor:" + deploymentException.getMessage());

            } catch (IOException ioException) {
                Log.d(TAG, "onConnectionError: IOException ao tentar conectar ao servidor: " + ioException.getMessage());

//        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException sslException) {
//            Log.d(TAG, "onConnectionError: Erro no handshake SSL ao tentar conectar ao servidor: " + sslException.getMessage());

            } catch (Exception genericException) {
                Log.d(TAG, "onConnectionError: Exception ao tentar conectar ao servidor: " + genericException.getMessage());
            } finally {
                if (isConnectionOpen())
                    Log.d(TAG, "Conectado ao servidor");
            }
        });

    }

    /**----------------------------------------------
    ------------------ WEBSOCKET API ----------------
    -----------------------------------------------*/
    @OnOpen
    public void onOpen(Session userSession, EndpointConfig config) {
        this.userSession = userSession;
        this.messageHandler.onConnectionOpen();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        Log.d(TAG, "Connection closed. Reason: " + reason.toString());
        this.userSession = null;
        clear();
    }

    @OnError
    public void onError(Throwable error, Session session) {
        Log.e(TAG, error.getMessage());
        clear();
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
        AppDefaultExecutor.getTalkingzLowPriorityNetworkingThreadPool().execute(() -> userSession.getAsyncRemote().sendObject(orchestration));
    }

    public void sendKeepAlivePing(){

        //check here because the caller maybe the Scheduler and in the mean time the connection can be closed.
        if (!isConnectionOpen()) return;

        ByteBuffer pingMsg = ByteBuffer.wrap(Long.valueOf(System.currentTimeMillis()).toString().getBytes());
        AppDefaultExecutor.getTalkingzLowPriorityNetworkingThreadPool().execute(() -> {
            try {
                Log.d(TAG, "Sending ping");
                userSession.getAsyncRemote().sendPing(pingMsg);
            } catch (IOException e) {
                Log.e(TAG,"Error on trying to ping.", e);
            }
        });
    }

    public interface TalkingzOrchestrationMessageHandler extends MessageHandler.Whole<Orchestration> {
        void onConnectionOpen();
    }
}