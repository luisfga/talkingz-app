package br.com.luisfga.talkingz.app.database.entity.message;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.sql.Timestamp;

import br.com.luisfga.talkingz.app.database.entity.group.Group;
import br.com.luisfga.talkingz.app.database.entity.user.User;

@Entity(tableName = "group_message",
        indices = {@Index("destination_group_id"),
                @Index("sender_id")},
        primaryKeys = {"sender_id","destination_group_id","sent_date"},
        foreignKeys ={@ForeignKey(entity = Group.class,
                parentColumns = "id",
                childColumns = "destination_group_id",
                onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "sender_id")})
public class GroupMessage {

    @NonNull
    @ColumnInfo(name = "sender_id")
    private long senderId;

    @NonNull
    @ColumnInfo(name = "destination_group_id")
    private long destinationGroupId;

    @NonNull
    @ColumnInfo(name = "sent_date")
    private Timestamp sentDate;

    private byte[] content;

    @ColumnInfo(name = "mime_type")
    private int mimeType;

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getMimeType() {
        return mimeType;
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }

    @NonNull
    public Timestamp getSentDate() {
        return sentDate;
    }

    public void setSentDate(@NonNull Timestamp sentDate) {
        this.sentDate = sentDate;
    }

    public long getDestinationGroupId() {
        return destinationGroupId;
    }

    public void setDestinationGroupId(long destinationGroupId) {
        this.destinationGroupId = destinationGroupId;
    }
}
