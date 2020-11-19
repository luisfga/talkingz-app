package br.com.luisfga.talkingz.app.database.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;

import br.com.luisfga.talkingz.app.database.OrchestraClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.dao.DirectMessageDAO;
import br.com.luisfga.talkingz.app.database.entity.message.DirectMessage;

public class DirectMessagesRepository {

    private Application application;
    private DirectMessageDAO directMessageDAO;
    private LiveData<List<DirectMessage>> mAllMessages;
    private UUID mainUserId;

    // Note that in order to unit test the ItemRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public DirectMessagesRepository(Application application, UUID contactId, UUID mainUserId) {
        this.application = application;
        this.mainUserId = mainUserId;
        OrchestraClientRoomDatabase db = OrchestraClientRoomDatabase.getDatabase(application);
        this.directMessageDAO = db.directMessageDAO();
        mAllMessages = this.directMessageDAO.listAll(contactId);
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<DirectMessage>> getmAllMessages() {
        return mAllMessages;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(DirectMessage directMessage) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.directMessageDAO.insert(directMessage));
    }

    public void update(DirectMessage directMessage) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.directMessageDAO.update(directMessage));
    }

    public void delete(DirectMessage directMessage) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.directMessageDAO.delete(directMessage));
    }

}