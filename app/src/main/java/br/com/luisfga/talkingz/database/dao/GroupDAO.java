package br.com.luisfga.talkingz.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.com.luisfga.talkingz.database.entity.Group;

@Dao
public interface GroupDAO {

    @Insert
    void insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT * FROM table_group ORDER BY id")
    LiveData<List<Group>> listAll();

//    @Query("SELECT * FROM contact WHERE id = :id")
//    Contact getContactById(long id);
}
