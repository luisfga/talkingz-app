package br.com.luisfga.talkingz.services.messaging.handling;

import android.app.Application;
import android.util.Log;
import br.com.luisfga.talkingz.TalkingzApp;
import br.com.luisfga.talkingz.database.entity.DirectMessage;
import br.com.luisfga.talkingz.services.messaging.MessagingWSClient;
import br.com.luisfga.talkingz.commons.MessageWrapper;
import br.com.luisfga.talkingz.commons.constants.MessageStatus;
import br.com.luisfga.talkingz.commons.orchestration.Orchestration;
import br.com.luisfga.talkingz.commons.orchestration.command.*;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandGetFile;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class TalkingzMessageHandler implements MessagingWSClient.TalkingzMessageHandler {

    private final String TAG = TalkingzMessageHandler.class.getSimpleName();

    private TalkingzApp talkingzApp;
    private MessagingWSClient messagingWSClient;
    private TalkingzResponseDispatcher talkingzResponseDispatcher;

    public TalkingzMessageHandler(MessagingWSClient messagingWSClient, Application app){
        this.messagingWSClient = messagingWSClient;
        this.talkingzApp = (TalkingzApp) app;
        this.talkingzResponseDispatcher = new TalkingzResponseDispatcher();
    }

    public TalkingzResponseDispatcher getTalkingzResponseDispatcher(){
        return this.talkingzResponseDispatcher;
    }

    /* -----------------------------------------------*/
    /* -------------- MESSAGE HANDLER API ------------*/
    /* -----------------------------------------------*/
    @Override
    public void onConnectionOpen() {
        Log.d(TAG, "onConnectionOpen");

        if (talkingzApp.getMainUser() != null) {
            //enviar pendências
            List<DirectMessage> pendentesDeEnvio = talkingzApp.getTalkingzDB().directMessageDAO().getByStatus(MessageStatus.MSG_STATUS_SENT);
            Log.d( TAG, "Há "+pendentesDeEnvio.size()+" mensagem(ens) pendente(s) de envio");
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

                Log.d( TAG, "enviando mensagem");
                messagingWSClient.sendCommandOrFeedBack(commandSend);

                //enviar arquivo de mídia, se for o caso
//                if (directMessage.getMediaUriPath() != null) {
//                    AppDefaultExecutor.getOrchestraLowPriorityNetworkingThreadPool().execute(() -> {
//                        try {
//                            fileTransferWSClient = new FileTransferWSClient(
//                                    FileTransferWSClient.ACTION_SEND, null,
//                                    directMessage.getMediaUriPath(), directMessage.getMimeType(), directMessage.getMediaDownloadToken());
//                            fileTransferWSClient.conectar(getApplicationContext());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//                }
            }
        }
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
            this.talkingzResponseDispatcher.getResponseCommandFindContactHandler().handleResponse((ResponseCommandFindContact) orchestration);

            //retorno tratado por atividade ou fragmento
        } else if (orchestration instanceof ResponseCommandGetFile) {
            this.talkingzResponseDispatcher.getResponseCommandGetFile().handleResponse((ResponseCommandGetFile) orchestration);

            //retorno meramente informativo sem relevância
        } else if (orchestration instanceof FeedBackCommandSyncUser) {
            Log.d( TAG, "Usuário sincronizado no servidor");
        }
    }

    /* -----------------------------------------------*/
    /* ------ MESSAGE HANDLING PRIVATE METHODS -------*/
    /* -----------------------------------------------*/
    private void processFeedBackCommandSend(FeedBackCommandSend feedBackCommandSend) {
        this.talkingzApp.getTalkingzDB().directMessageDAO().updateAfterFeedBack(
                feedBackCommandSend.getId(),
                new Timestamp(feedBackCommandSend.getSentTimeInMillis()),
                MessageStatus.MSG_STATUS_ON_TRAFFIC);
        Log.d( TAG, "FeedBack received: FeedBackCommandSend");
    }

    private void processCommandConfirmDelivery(CommandConfirmDelivery commandConfirmDelivery) {
        this.talkingzApp.getTalkingzDB().directMessageDAO().updateMessageStatus(commandConfirmDelivery.getId(), MessageStatus.MSG_STATUS_DELIVERED);

        Log.d( TAG, "Confirmação recebida da mensagem: " + commandConfirmDelivery.getId());

        FeedBackCommandConfirmDelivery feedBackCommandConfirmDelivery = new FeedBackCommandConfirmDelivery();
        feedBackCommandConfirmDelivery.setId(commandConfirmDelivery.getId());
        Log.d( TAG, "Enviando FeedBackCommandConfirmDelivery da mensagem: " + feedBackCommandConfirmDelivery.getId());
        this.messagingWSClient.sendCommandOrFeedBack(feedBackCommandConfirmDelivery);
    }

    private void processFeedBackCommandOnLogin(FeedBackCommandLogin feedBackCommandLogin) {
        Log.d( TAG, "processFeedBackCommandOnLogin");

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
            this.talkingzApp.getTalkingzDB().directMessageDAO().insert(directMessage);
            Log.d( TAG, "Mensagem recebida: " + directMessage.getContent());

            FeedBackCommandDeliver feedBackMessageReceived = new FeedBackCommandDeliver();
            feedBackMessageReceived.setSenderId(directMessage.getSenderId());
            feedBackMessageReceived.setId(directMessage.getId());
            Log.d( TAG, "Enviando FeedBackCommandDeliver da mensagem: " + feedBackMessageReceived.getId());
            this.messagingWSClient.sendCommandOrFeedBack(feedBackMessageReceived);
        }

        for (UUID uuid : feedBackCommandLogin.getPendingConfirmationUUIDs()) {
            this.talkingzApp.getTalkingzDB().directMessageDAO().updateMessageStatus(uuid, MessageStatus.MSG_STATUS_DELIVERED);

            FeedBackCommandConfirmDelivery feedBackCommandConfirmDelivery = new FeedBackCommandConfirmDelivery();
            feedBackCommandConfirmDelivery.setId(uuid);

            Log.d( TAG, "Enviando FeedBackCommandConfirmDelivery da mensagem: " + uuid);
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
        this.talkingzApp.getTalkingzDB().directMessageDAO().insert(directMessage);
        Log.d( TAG, "Mensagem recebida: " + directMessage.getContent());

        //send feedback
        FeedBackCommandDeliver feedBackCommandDeliver = new FeedBackCommandDeliver();
        feedBackCommandDeliver.setSenderId(directMessage.getSenderId());
        feedBackCommandDeliver.setId(directMessage.getId());
        this.messagingWSClient.sendCommandOrFeedBack(feedBackCommandDeliver);
        Log.d( TAG, "Enviando feedBackCommandDeliver da mensagem: " + directMessage.getId());
    }

}
