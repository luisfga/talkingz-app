package br.com.luisfga.talkingz.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import br.com.luisfga.talkingz.app.database.OrchestraClientRoomDatabase;
import br.com.luisfga.talkingz.app.database.entity.message.DirectMessage;

@Dao
public abstract class DirectMessageDAO implements BaseDAO<DirectMessage> {

    @Query("SELECT * FROM direct_message WHERE dest_id = :contactId OR sender_id = :contactId ORDER BY sent_time ASC")
    public abstract LiveData<List<DirectMessage>> listAll(UUID contactId);

    @Query("SELECT * FROM direct_message WHERE id = :id")
    public abstract DirectMessage getByUUID(UUID id);

    @Query("SELECT * FROM direct_message WHERE status = :status")
    public abstract List<DirectMessage> getByStatus(int status);

    @Update
    public void updateAfterFeedBack(UUID uuid, Timestamp sentTime, int status) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> {
            DirectMessage directMessage = getByUUID(uuid);
            directMessage.setSentTime(sentTime);
            directMessage.setStatus(status);
            update(directMessage);
        });
    }

    @Update
    public void updateMessageStatus(UUID uuid, int status) {
        OrchestraClientRoomDatabase.getDatabaseWriteExecutor().execute(() -> {
            DirectMessage directMessage = getByUUID(uuid);
            directMessage.setStatus(status);
            update(directMessage);
        });
    }
//    @Query("SELECT * FROM direct_message WHERE id = :id")
//    Contact getById(long id);
}
