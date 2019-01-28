package com.murphy.pokotalk.data.user;

public class User {
    protected int userId;
    protected String email;
    protected String nickname;
    protected String picture;

    public User() {

    }

    public User(int id, String email, String nickname, String picture) {
        this.userId = id;
        this.email = email;
        this.nickname = nickname;
        this.picture = picture;
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

    public void update(User user) {
        setEmail(user.getEmail());
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
