package br.com.luisfga.talkingz.app.database.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import br.com.luisfga.talkingz.app.database.entity.user.User;
import br.com.luisfga.talkingz.app.database.repositories.ContactsRepository;

public class ContactViewModel extends AndroidViewModel {

    //Repository reference
    private ContactsRepository mRepository;

    //List that will point to repository cached list
    private LiveData<List<User>> mAllContacts;

    public ContactViewModel(Application application) {
        super(application);
        //create items repo
        mRepository = new ContactsRepository(application);
        //get the viewModel's list pointing to that cached on ItemRepository
        mAllContacts = mRepository.getmAllContacts();
    }

    //Return cached list
    public LiveData<List<User>> getAllContact() {
        return mAllContacts;
    }

    //save new item
    public void insert(User contact) {
        mRepository.insert(contact);
    }

    //delete a item
    public void delete(User contact) {
        mRepository.delete(contact);
    }

    //update existing item
    public void update(User contact) {
        mRepository.update(contact);
    }

}
