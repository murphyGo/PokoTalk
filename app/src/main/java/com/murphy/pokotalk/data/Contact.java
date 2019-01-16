package com.murphy.pokotalk.data;

import java.util.Calendar;

public class Contact {
    private String email;
    private String nickname;
    private String picture;
    private Calendar lastSeen;

    public Contact(String email, String nickname, String picture, Calendar lastSeen) {
        this.email = email;
        this.nickname = nickname;
        this.picture = picture;
        this.lastSeen = lastSeen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }
}
