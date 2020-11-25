package br.com.luisfga.talkingz.app.core;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import android.widget.Toast;
import androidx.annotation.WorkerThread;
import br.com.luisfga.talkingz.app.core.services.messaging.FileTransferWSClient;
import br.com.luisfga.talkingz.app.database.TalkingzClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.entity.DirectMessage;
import br.com.luisfga.talkingz.app.database.entity.User;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.commons.constants.MessageStatus;
import br.com.luisfga.talkingz.commons.MessageWrapper;
import br.com.luisfga.talkingz.commons.orchestration.Orchestration;
import br.com.luisfga.talkingz.commons.orchestration.command.CommandConfirmDelivery;
import br.com.luisfga.talkingz.commons.orchestration.command.CommandDeliver;
import br.com.luisfga.talkingz.commons.orchestration.command.CommandSend;
import br.com.luisfga.talkingz.commons.orchestration.command.FeedBackCommandConfirmDelivery;
import br.com.luisfga.talkingz.commons.orchestration.command.FeedBackCommandDeliver;
import br.com.luisfga.talkingz.commons.orchestration.command.FeedBackCommandLogin;
import br.com.luisfga.talkingz.commons.orchestration.command.FeedBackCommandSend;
import br.com.luisfga.talkingz.commons.orchestration.command.FeedBackCommandSyncUser;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandGetFile;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseDispatcher;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseHandler;

/**
 * @author luisfga
 */
public class TalkingzApp extends Application implements MessagingWSClient.OrchestraMessageHandler, ResponseDispatcher{

    private final String TAG = "TalkinzApp";

    //BANCO DE DADOS
    public TalkingzClientRoomDatabase getTalkingzDB() {
        return TalkingzClientRoomDatabase.getDatabase(this);
    }

    //USUÁRIO
    private User mainUser;
    public User getMainUser() {
        return mainUser;
    }

    //WEBSOCKET para mensagens
    private MessagingWSClient messagingWSClient;
    public MessagingWSClient getWsClient() {
        return this.messagingWSClient;
    }

    //WEBSOCKET para transferência de arquivos
    private FileTransferWSClient fileTransferWSClient;

    /* -----------------------------------------------*/
    /* ----------- CONFIG AND INITIALIZATION ---------*/
    /* -----------------------------------------------*/

    @WorkerThread
    private void loadUser() {

            mainUser = getTalkingzDB().userDAO().getMainUser();

            if (mainUser == null) {
                mainUser = new User();
                mainUser.setMainUser(true);
                mainUser.setId(UUID.randomUUID());
                mainUser.setName("");
                mainUser.setEmail("");
                mainUser.setSearchToken("");
                mainUser.setJoinTime(System.currentTimeMillis());

                getTalkingzDB().userDAO().insert(mainUser);

                Looper.prepare();
                Toast.makeText(this, "Novo usuário criado: " + mainUser.getId().toString(), Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().execute(() -> {
            loadUser();
//            connect();
        });
//        Intent companionServiceIntent = new Intent(this, TalkingzCompanionService.class);
//        startService(companionServiceIntent);
    }

    /* -----------------------------------------------*/
    /* -------------- MÉTODOS PÚBLICOS ---------------*/
    /* -----------------------------------------------*/
    public void connect() {
        this.messagingWSClient = MessagingWSClient.getIntansce(this);

        if (this.mainUser != null && !this.messagingWSClient.isConnectionOpen())
            this.messagingWSClient.conectar(this.getApplicationContext(), this.mainUser.getId().toString());
    }

    public boolean isInternetAvailable() {
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

    /* -----------------------------------------------*/
    /* -------------- MESSAGE HANDLER API ------------*/
    /* -----------------------------------------------*/
    public boolean isConnectionOpen() {
        return MessagingWSClient.getIntansce(this).isConnectionOpen();
    }

    @Override
    public void onConnectionOpen() {
        Log.println(Log.DEBUG, TAG, "onConnectionOpen");

        if (getMainUser() != null) {
            //enviar pendências
            List<DirectMessage> pendentesDeEnvio = getTalkingzDB().directMessageDAO().getByStatus(MessageStatus.MSG_STATUS_SENT);
            Log.println(Log.DEBUG, TAG, "Há "+pendentesDeEnvio.size()+" mensagem(ens) pendente(s) de envio");
            for (DirectMessage directMessage: pendentesDeEnvio) {

                MessageWrapper messageWrapper = new MessageWrapper();

                messageWrapper.setId(directMessage.getId());
                messageWrapper.setSenderId(directMessage.getSenderId().toString());
                messageWrapper.setDestId(directMessage.getDestId().toString());
                messageWrapper.setContent(directMessage.getContent());
                messageWrapper.setMimetype(directMessage.getMimeType());
                messageWrapper.setDownloadToken(directMessage.getMediaDownloadToken());
                messageWrapper.setMediaThumbnail(directMessage.getMediaThumbnail());

                CommandSend commandSend = new CommandSend();
                commandSend.setMessageWrapper(messageWrapper);

                Log.println(Log.DEBUG, TAG, "enviando mensagem");
                this.messagingWSClient.sendCommandOrFeedBack(commandSend);

                //enviar arquivo de mídia, se for o caso
                if (directMessage.getMediaUriPath() != null) {
                    AppDefaultExecutor.getOrchestraLowPriorityNetworkingThreadPool().execute(() -> {
                        try {
                            fileTransferWSClient = new FileTransferWSClient(
                                    FileTransferWSClient.ACTION_SEND, null,
                                    directMessage.getMediaUriPath(), directMessage.getMimeType(), directMessage.getMediaDownloadToken());
                            fileTransferWSClient.conectar(getApplicationContext());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onConnectionClose() {
        Log.println(Log.DEBUG, TAG, "onConnectionClose");
        MessagingWSClient.clear();
    }

    @Override
    public void onConnectionError(String errorMessage) {
        Log.println(Log.DEBUG, TAG, "onConnectionError");
        Log.println(Log.DEBUG, TAG, errorMessage);
        MessagingWSClient.clear();
    }

    @Override
    public void onMessage(Orchestration orchestration) {
        if (orchestration instanceof FeedBackCommandSend) {
            processFeedBackCommandSend((FeedBackCommandSend) orchestration);

        } else if (orchestration instanceof FeedBackCommandLogin) {
            processFeedBackCommandOnLogin((FeedBackCommandLogin) orchestration);

        } else if (orchestration instanceof CommandDeliver) {
            processCommandDeliver((CommandDeliver) orchestration);

        } else if (orchestration instanceof CommandConfirmDelivery) {
            processCommandConfirmDelivery((CommandConfirmDelivery) orchestration);

            //retorno tratado por atividade ou fragmento
        } else if (orchestration instanceof ResponseCommandFindContact) {
            this.responseCommandFindContactHandler.handleResponse((ResponseCommandFindContact) orchestration);

            //retorno tratado por atividade ou fragmento
        } else if (orchestration instanceof ResponseCommandGetFile) {
            this.responseCommandGetFile.handleResponse((ResponseCommandGetFile) orchestration);

            //retorno meramente informativo sem relevância
        } else if (orchestration instanceof FeedBackCommandSyncUser) {
            Log.println(Log.DEBUG, TAG, "Usuário sincronizado no servidor");
        }
    }

    /* -----------------------------------------------*/
    /* ------ MESSAGE HANDLING PRIVATE METHODS -------*/
    /* -----------------------------------------------*/
    private void processFeedBackCommandSend(FeedBackCommandSend feedBackCommandSend) {
        getTalkingzDB().directMessageDAO().updateAfterFeedBack(
                feedBackCommandSend.getId(),
                new Timestamp(feedBackCommandSend.getSentTimeInMillis()),
                MessageStatus.MSG_STATUS_ON_TRAFFIC);
        Log.println(Log.DEBUG, TAG, "FeedBack recebido: FeedBackCommandSend");
    }

    private void processCommandConfirmDelivery(CommandConfirmDelivery commandConfirmDelivery) {
        getTalkingzDB().directMessageDAO().updateMessageStatus(commandConfirmDelivery.getId(), MessageStatus.MSG_STATUS_DELIVERED);

        Log.println(Log.DEBUG, TAG, "Confirmação recebida da mensagem: " + commandConfirmDelivery.getId());

        FeedBackCommandConfirmDelivery feedBackCommandConfirmDelivery = new FeedBackCommandConfirmDelivery();
        feedBackCommandConfirmDelivery.setId(commandConfirmDelivery.getId());
        Log.println(Log.DEBUG, TAG, "Enviando FeedBackCommandConfirmDelivery da mensagem: " + feedBackCommandConfirmDelivery.getId());
        this.messagingWSClient.sendCommandOrFeedBack(feedBackCommandConfirmDelivery);
    }

    private void processFeedBackCommandOnLogin(FeedBackCommandLogin feedBackCommandLogin) {
        Log.println(Log.DEBUG, TAG, "processFeedBackCommandOnLogin");

        for (MessageWrapper messageWrapper : feedBackCommandLogin.getPendingMessages()) {
            DirectMessage directMessage = new DirectMessage();

            //save message
            directMessage.setId(messageWrapper.getId());
            directMessage.setSenderId(UUID.fromString(messageWrapper.getSenderId()));
            directMessage.setDestId(UUID.fromString(messageWrapper.getDestId()));
            directMessage.setSentTime(new Timestamp(messageWrapper.getSentTimeInMilis()));
            directMessage.setContent(messageWrapper.getContent());
            directMessage.setMimeType(messageWrapper.getMimetype());
            directMessage.setMediaDownloadToken(messageWrapper.getDownloadToken());
            directMessage.setMediaThumbnail(messageWrapper.getMediaThumbnail());

            directMessage.setStatus(MessageStatus.MSG_STATUS_RECEIVED); //salva localmente com status RECEBIDA
            getTalkingzDB().directMessageDAO().insert(directMessage);
            Log.println(Log.DEBUG, TAG, "Mensagem recebida: " + directMessage.getContent());

            FeedBackCommandDeliver feedBackMessageReceived = new FeedBackCommandDeliver();
            feedBackMessageReceived.setSenderId(directMessage.getSenderId());
            feedBackMessageReceived.setId(directMessage.getId());
            Log.println(Log.DEBUG, TAG, "Enviando FeedBackCommandDeliver da mensagem: " + feedBackMessageReceived.getId());
            this.messagingWSClient.sendCommandOrFeedBack(feedBackMessageReceived);
        }

        for (UUID uuid : feedBackCommandLogin.getPendingConfirmationUUIDs()) {
            getTalkingzDB().directMessageDAO().updateMessageStatus(uuid, MessageStatus.MSG_STATUS_DELIVERED);

            FeedBackCommandConfirmDelivery feedBackCommandConfirmDelivery = new FeedBackCommandConfirmDelivery();
            feedBackCommandConfirmDelivery.setId(uuid);

            Log.println(Log.DEBUG, TAG, "Enviando FeedBackCommandConfirmDelivery da mensagem: " + uuid);
            this.messagingWSClient.sendCommandOrFeedBack(feedBackCommandConfirmDelivery);
        }
    }

    private void processCommandDeliver(CommandDeliver commandDeliver) {
        DirectMessage directMessage = new DirectMessage();

        //save message
        directMessage.setId(commandDeliver.getMessageWrapper().getId());
        directMessage.setSenderId(UUID.fromString(commandDeliver.getMessageWrapper().getSenderId()));
        directMessage.setDestId(UUID.fromString(commandDeliver.getMessageWrapper().getDestId()));
        directMessage.setSentTime(new Timestamp(commandDeliver.getMessageWrapper().getSentTimeInMilis()));
        directMessage.setContent(commandDeliver.getMessageWrapper().getContent());
        directMessage.setMimeType(commandDeliver.getMessageWrapper().getMimetype());
        directMessage.setMediaDownloadToken(commandDeliver.getMessageWrapper().getDownloadToken());
        directMessage.setMediaThumbnail(commandDeliver.getMessageWrapper().getMediaThumbnail());

        directMessage.setStatus(MessageStatus.MSG_STATUS_RECEIVED); //salva localmente com status RECEBIDA
        getTalkingzDB().directMessageDAO().insert(directMessage);
        Log.println(Log.DEBUG, TAG, "Mensagem recebida: " + directMessage.getContent());

        //send feedback
        FeedBackCommandDeliver feedBackCommandDeliver = new FeedBackCommandDeliver();
        feedBackCommandDeliver.setSenderId(directMessage.getSenderId());
        feedBackCommandDeliver.setId(directMessage.getId());
        this.messagingWSClient.sendCommandOrFeedBack(feedBackCommandDeliver);
        Log.println(Log.DEBUG, TAG, "Enviando feedBackCommandDeliver da mensagem: " + directMessage.getId());
    }

    /* -----------------------------------------------*/
    /* ----------- RESPONSE DISPATCHER API -----------*/
    /* -----------------------------------------------*/
    //jeito de entregar respostas aguardadas para atividades e fragmentos

    private ResponseHandler<ResponseCommandFindContact> responseCommandFindContactHandler;
    private ResponseHandler<ResponseCommandGetFile> responseCommandGetFile;
    @Override
    public void setResponseCommandFindContactHandler(ResponseHandler<ResponseCommandFindContact> responseHandler) {
        this.responseCommandFindContactHandler = responseHandler;
    }

    @Override
    public void setResponseCommandGetFileHandler(ResponseHandler<ResponseCommandGetFile> responseHandler) {
        this.responseCommandGetFile = responseHandler;
    }

}
