package br.com.luisfga.talkingz.app.background;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.app.utils.FileUtility;
import br.com.luisfga.talkingz.commons.orchestration.Orchestration;
import br.com.luisfga.talkingz.commons.orchestration.OrchestrationDecoder;
import br.com.luisfga.talkingz.commons.orchestration.OrchestrationEncoder;
import br.com.luisfga.talkingz.commons.orchestration.response.CommandGetFile;
import br.com.luisfga.talkingz.commons.utils.SharedFileDTO;
import br.com.luisfga.talkingz.commons.utils.TransferUtility;

@ClientEndpoint(
        decoders = {OrchestrationDecoder.class},
        encoders = {OrchestrationEncoder.class})
public class FileTransferWSClient {

    public static final int ACTION_SEND = 0;
    public static final int ACTION_GET = 1;

    private final String TAG = "FileTransferWSClient";
    private Session userSession = null;

    private Context applicationContext;

    private CommandGetFile commandGetFile;
    private String mediaFileUriPath;
    private byte mimeType;
    private String downloadToken;

    private int actionSelected;

    //já cria o cliente carregado com todas as informações necessárias, depois é só executar o comando e fechar a conexao
    public FileTransferWSClient(@NonNull int action, CommandGetFile cmdGetFile, String mediaFileUriPath, byte mimeType, String downloadToken) throws Exception {
        this.actionSelected = action;
        //validação dos informações necessárias
        switch (actionSelected){
            case ACTION_GET:
                if (cmdGetFile == null) throw new Exception("CommandGetFile não pode ser nulo para a ação GET");
                if (cmdGetFile.getDownloadToken() == null || "".equals(cmdGetFile.getDownloadToken()))  throw new Exception("DownloadToken não pode ser nulo para a ação GET");
                this.commandGetFile = cmdGetFile;
                break;
            case ACTION_SEND:
                //3 são os parêmetros esperados ([0]MediaUriPath, [1]MimeType, [2]MediaDownloadToken)
                if (mediaFileUriPath == null || mediaFileUriPath.equals("")) throw new Exception("Faltando o caminho do arquivo para a ação SEND");
                if (mimeType == 0) throw new Exception("Faltando o mime type para a ação SEND");
                if (downloadToken == null || downloadToken.equals("")) throw new Exception("Faltando o download token para a ação SEND");
                this.mediaFileUriPath = mediaFileUriPath;
                this.mimeType = mimeType;
                this.downloadToken = downloadToken;
                break;
        }
    }

    public void conectar(Context applicationContext) {
        this.applicationContext = applicationContext;
        try {
            URI remoteURI = new URI("wss://192.168.0.2:8443/OrchestraWebapp/FileTransferWSEndpoint");
//            URI remoteURI = new URI("wss://luisfga-49447.portmap.host:48335/OrchestraWebapp/FileTransferWSEndpoint");

            ClientManager clientManager = ClientManager.createClient();
            clientManager.getProperties().put("org.glassfish.tyrus.incomingBufferSize", 110000000);
            clientManager.setAsyncSendTimeout(-1);

            //SSL Configuration
            SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(SSLUtility.getConfiguredSSLContext(applicationContext), true, false, false);
            sslEngineConfigurator.setHostVerificationEnabled(false);
            clientManager.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);

            clientManager.connectToServer(this, remoteURI);

            Log.println(Log.INFO, TAG, "Conectado ao servidor");

        } catch (URISyntaxException uriSyntaxException) {
            Log.e(TAG, "URISyntaxException ao tentar criar canal de transferência: "+ uriSyntaxException.getMessage());

        } catch (DeploymentException deploymentException) {
            Log.e(TAG, "DeploymentException ao tentar conectar ao servidor: " + deploymentException.getMessage());

        } catch (IOException ioException) {
            Log.e(TAG, "IOException ao tentar conectar ao servidor: " + ioException.getMessage());

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException sslException) {
            Log.e(TAG, "Erro no handshake SSL ao tentar conectar ao servidor: " + sslException.getMessage());

        } catch (Exception genericException) {
            Log.e(TAG, "Exception ao tentar conectar ao servidor. " + genericException.getMessage());
        }
    }

    /**************
     * WEBSOCKET API - implementação de métodos abstratos
     **************/
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
        if (actionSelected == ACTION_GET) sendCommandOrFeedBack(this.commandGetFile);
        else if (actionSelected == ACTION_SEND) sendSharedFile();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        Log.println(Log.INFO, TAG, "Conexão fechada. Reason: " + reason.toString());
    }

    @OnError
    public void onError(Throwable error, Session session) {
        Log.e(TAG, error.getMessage());
    }

    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, Session session) {
        SharedFileDTO sharedFileDTO = TransferUtility.getSharedFileDTOFromByteBuffer(byteBuffer);
        Uri uri = FileUtility.getOutputMediaFileUri(applicationContext, sharedFileDTO.getMimeType(), sharedFileDTO.getDownloadToken());
        try {
            OutputStream fos = applicationContext.getContentResolver().openOutputStream(uri);
            fos.write(sharedFileDTO.getBytes());
            fos.close();
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Download concluído"));
            Log.i(TAG, "Download concluído");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(Orchestration orchestration) {
    }

    public void sendCommandOrFeedBack(Orchestration orchestration) {
        AppDefaultExecutor.getOrchestraLowPriorityNetworkingThreadPool().execute(() -> userSession.getAsyncRemote().sendObject(orchestration));
    }

    private void sendSharedFile() {
        AppDefaultExecutor.getOrchestraLowPriorityNetworkingThreadPool().execute(() -> {
            try {
                Uri mediaUri = Uri.parse(mediaFileUriPath);
                InputStream mediaInputStream = applicationContext.getContentResolver().openInputStream(mediaUri);
                ByteBuffer byteBuffer = TransferUtility.getByteBufferFromInputStreamToTransfer(mediaInputStream, mimeType, downloadToken);
                userSession.getAsyncRemote().sendBinary(byteBuffer, new BinarySendHandler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private class BinarySendHandler implements SendHandler {
        @Override
        public void onResult(SendResult result) {
            Log.i(TAG, "onResult: Arquivo enviado");
        }
    }
}