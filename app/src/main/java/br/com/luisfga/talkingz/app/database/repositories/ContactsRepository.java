package br.com.luisfga.talkingz.app.database.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import br.com.luisfga.talkingz.app.database.OrchestraClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.dao.UserDAO;
import br.com.luisfga.talkingz.app.database.entity.user.User;

public class ContactsRepository {

    private UserDAO userDAO;
    private LiveData<List<User>> mAllContacts;

    // Note that in order to unit test the ItemRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public ContactsRepository(Application application) {
        OrchestraClientRoomDatabase db = OrchestraClientRoomDatabase.getDatabase(application);
        this.userDAO = db.userDAO();
        mAllContacts = this.userDAO.listAllContacts();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<User>> getmAllContacts() {
        return mAllContacts;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(User contact) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.userDAO.insert(contact));
    }

    public void update(User contact) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.userDAO.update(contact));
    }

    public void delete(User contact) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.userDAO.delete(contact));
    }

}