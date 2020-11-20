package br.com.luisfga.talkingz.app.database.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import br.com.luisfga.talkingz.app.database.TalkingzClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.dao.GroupDAO;
import br.com.luisfga.talkingz.app.database.entity.group.Group;

public class GroupsRepository {

    private GroupDAO groupDAO;
    private LiveData<List<Group>> mAllGroups;


    // Note that in order to unit test the ItemRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public GroupsRepository(Application application) {
        TalkingzClientRoomDatabase db = TalkingzClientRoomDatabase.getDatabase(application);
        this.groupDAO = db.groupDAO();
        mAllGroups = this.groupDAO.listAll();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Group>> getmAllGroups() {
        return mAllGroups;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Group group) {
        TalkingzClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.groupDAO.insert(group));
    }

    public void update(Group group) {
        TalkingzClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.groupDAO.update(group));
    }

    public void delete(Group group) {
        TalkingzClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> this.groupDAO.delete(group));
    }
}