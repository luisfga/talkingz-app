package br.com.luisfga.talkingz.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "user_group_join",
        indices = @Index("group_id"),
        primaryKeys = {"user_id","group_id"},
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id"),
                @ForeignKey(entity = Group.class,
                        parentColumns = "id",
                        childColumns = "group_id")
        })
public class UserGroupJoin {


    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "group_id")
    private long groupId;

    @ColumnInfo(name = "is_admin")
    private boolean isAdmin;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
