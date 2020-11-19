package br.com.luisfga.talkingz.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import br.com.luisfga.talkingz.app.database.entity.user.User;

@Dao
public interface UserDAO {

    @Insert
    void insert(User contact);

    @Update
    void update(User contact);

    @Delete
    void delete(User contact);

    @Query("SELECT * FROM user WHERE is_main_user = 1 LIMIT 1")
    User getMainUser();

    @Query("SELECT * FROM user WHERE is_main_user = 0 ORDER BY name")
    LiveData<List<User>> listAllContacts();

    @Query("SELECT * FROM user WHERE search_token = :searchToken")
    User getByToken(String searchToken);

    @Query("SELECT * FROM user WHERE id = :id")
    User getById(UUID id);
}
