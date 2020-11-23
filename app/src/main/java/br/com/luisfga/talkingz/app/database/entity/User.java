package br.com.luisfga.talkingz.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity
public class User {

    @PrimaryKey @NonNull
    private UUID id;

    @ColumnInfo(name = "search_token")
    private String searchToken;

    @ColumnInfo(name = "join_time")
    private Long joinTime;

    private String name;
    private String email;

    private byte[] thumbnail;



    @ColumnInfo(name = "is_main_user")
    private boolean isMainUser = false;

    //GETTERs and SETTERs ----------------------------------------------------------
    //------------------------------------------------------------------------------
    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getSearchToken() {
        return searchToken;
    }

    public void setSearchToken(String searchToken) {
        this.searchToken = searchToken;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isMainUser() {
        return isMainUser;
    }

    public void setMainUser(boolean mainUser) {
        isMainUser = mainUser;
    }
}