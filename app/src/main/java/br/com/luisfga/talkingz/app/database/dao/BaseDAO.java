package br.com.luisfga.talkingz.app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

@Dao
public interface BaseDAO<T> {

    @Insert
    long insert(T obj);

    @Update
    void update(T obj);

    @Delete
    void delete(T obj);

}
