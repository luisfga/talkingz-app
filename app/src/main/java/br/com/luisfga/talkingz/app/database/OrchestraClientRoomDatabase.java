package br.com.luisfga.talkingz.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.luisfga.talkingz.app.database.dao.UserDAO;
import br.com.luisfga.talkingz.app.database.dao.DirectMessageDAO;
import br.com.luisfga.talkingz.app.database.dao.GroupDAO;
import br.com.luisfga.talkingz.app.database.entity.UserGroupJoin;
import br.com.luisfga.talkingz.app.database.entity.group.Group;
import br.com.luisfga.talkingz.app.database.entity.message.DirectMessage;
import br.com.luisfga.talkingz.app.database.entity.message.GroupMessage;
import br.com.luisfga.talkingz.app.database.entity.user.User;

@Database(entities = {
        User.class,
        Group.class,
        DirectMessage.class,
        GroupMessage.class,
        UserGroupJoin.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class OrchestraClientRoomDatabase extends RoomDatabase {

    public abstract UserDAO userDAO();//MainUser and Contacts
    public abstract GroupDAO groupDAO();
    public abstract DirectMessageDAO directMessageDAO();

    /**
     * DO NOT USE THIS, UNLESS YOU KNOW WHAT YOU ARE DOING
     */
    private static volatile OrchestraClientRoomDatabase THREAD_UNSAFE_DANGEROUS_INSTANCE;

    private static volatile OrchestraClientRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ExecutorService getDatabaseWriteExecutor(){
        return databaseWriteExecutor;
    }

    public static OrchestraClientRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (OrchestraClientRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            OrchestraClientRoomDatabase.class, "orchestraclient_database")
                            .build();
                }
            }
        }

        return INSTANCE;
    }

}
