package br.com.luisfga.talkingz.services.messaging.handling;

import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandGetFile;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseDispatcher;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseHandler;

public class TalkingzResponseDispatcher implements ResponseDispatcher {

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

    public ResponseHandler<ResponseCommandFindContact> getResponseCommandFindContactHandler() {
        return responseCommandFindContactHandler;
    }

    @Override
    public void setResponseCommandGetFileHandler(ResponseHandler<ResponseCommandGetFile> responseHandler) {
        this.responseCommandGetFile = responseHandler;
    }

    public ResponseHandler<ResponseCommandGetFile> getResponseCommandGetFile() {
        return responseCommandGetFile;
    }
}
