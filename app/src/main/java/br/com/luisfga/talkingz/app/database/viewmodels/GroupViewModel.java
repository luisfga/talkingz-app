package br.com.luisfga.talkingz.app.database.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import br.com.luisfga.talkingz.app.database.entity.Group;
import br.com.luisfga.talkingz.app.database.repositories.GroupsRepository;

public class GroupViewModel extends AndroidViewModel {

    //Repository reference
    private GroupsRepository mRepository;

    //List that will point to repository cached list
    private LiveData<List<Group>> mAllGroups;

    public GroupViewModel(Application application) {
        super(application);
        //create items repo
        mRepository = new GroupsRepository(application);
        //get the viewModel's list pointing to that cached on ItemRepository
        mAllGroups = mRepository.getmAllGroups();
    }

    //Return cached list
    public LiveData<List<Group>> getAllGroups() {
        return mAllGroups;
    }

    //save new item
    public void insert(Group group) {
        mRepository.insert(group);
    }

    //delete a item
    public void delete(Group group) {
        mRepository.delete(group);
    }

    //update existing item
    public void update(Group group) {
        mRepository.update(group);
    }

}
