package br.com.luisfga.talkingz.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.util.UUID;

@Entity(tableName = "direct_message",
        indices = {@Index("dest_id"),
                   @Index("sender_id")},
        foreignKeys ={@ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "dest_id",
                        onDelete = ForeignKey.CASCADE)})
public class DirectMessage {

    @PrimaryKey @NonNull
    private UUID id;

    @ColumnInfo(name = "sender_id")
    private UUID senderId;

    @ColumnInfo(name = "dest_id")
    private UUID destId;

    @ColumnInfo(name = "sent_time")
    private Timestamp sentTime;

    private String content;

    @ColumnInfo(name = "mime_type")
    private byte mimeType;

    private byte[] mediaThumbnail;

    private String mediaUriPath;

    @ColumnInfo(name = "download_token")
    private String mediaDownloadToken; //usado para identificar o arquivo no servidor, quando compartilhado. Através desse token o destinatário poderá fazer o download.

    private int status;

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getDestId() {
        return destId;
    }

    public void setDestId(UUID destId) {
        this.destId = destId;
    }

    public Timestamp getSentTime() {
        return sentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        this.sentTime = sentTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte getMimeType() {
        return mimeType;
    }

    public void setMimeType(byte mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getMediaThumbnail() {
        return mediaThumbnail;
    }

    public void setMediaThumbnail(byte[] mediaThumbnail) {
        this.mediaThumbnail = mediaThumbnail;
    }

    public String getMediaUriPath() {
        return mediaUriPath;
    }

    public void setMediaUriPath(String mediaUriPath) {
        this.mediaUriPath = mediaUriPath;
    }

    public String getMediaDownloadToken() {
        return mediaDownloadToken;
    }

    public void setMediaDownloadToken(String mediaDownloadToken) {
        this.mediaDownloadToken = mediaDownloadToken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
