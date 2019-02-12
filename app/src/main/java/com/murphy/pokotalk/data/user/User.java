package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.Message;

import java.util.ArrayList;

public class User extends Item {
    protected int userId;
    protected String email;
    protected String nickname;
    protected String picture;
    protected ArrayList<Message> messageList;
    protected ArrayList<Group> groupList;

    public User() {
        initList();
    }

    public User(int id, String email, String nickname, String picture) {
        this.userId = id;
        this.email = email;
        this.nickname = nickname;
        this.picture = picture;
        initList();
    }

    protected void initList() {
        messageList = new ArrayList<>();
        groupList = new ArrayList<>();
    }

    @Override
    public boolean equals(Object i) {
        if (i instanceof Integer) {
            return userId == (Integer) i;
        } else if (i instanceof User) {
            return userId == ((User) i).getUserId();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return userId;
    }

    @Override
    public void update(Item item) {
        User user = (User) item;
        setNickname(user.getNickname());
        setPicture(user.getPicture());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int id) {
        this.userId = id;
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
}
