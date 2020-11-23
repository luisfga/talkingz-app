package br.com.luisfga.talkingz.app.database.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;

import br.com.luisfga.talkingz.app.database.entity.DirectMessage;
import br.com.luisfga.talkingz.app.database.repositories.DirectMessagesRepository;

public class DirectMessageViewModel extends AndroidViewModel {

    //Repository reference
    private DirectMessagesRepository mRepository;

    //List that will point to repository cached list
    private LiveData<List<DirectMessage>> mAllMessages;

    public DirectMessageViewModel(Application application, UUID contactId, UUID mainUserId) {
        super(application);
        //create items repo
        mRepository = new DirectMessagesRepository(application, contactId, mainUserId);
        //get the viewModel's list pointing to that cached on ItemRepository
        mAllMessages = mRepository.getmAllMessages();
    }

    //Return cached list
    public LiveData<List<DirectMessage>> getAllMessages() {
        return mAllMessages;
    }

    //save new item
    public void insert(DirectMessage directMessage) {
        mRepository.insert(directMessage);
    }

    //delete a item
    public void delete(DirectMessage directMessage) {
        mRepository.delete(directMessage);
    }

    //update existing item
    public void update(DirectMessage directMessage) {
        mRepository.update(directMessage);
    }

}
