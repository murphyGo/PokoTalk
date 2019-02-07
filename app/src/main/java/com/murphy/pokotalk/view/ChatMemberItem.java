package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.user.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMemberItem extends FrameLayout {
    private String nickname;
    private String email;
    private String img;
    private Context context;
    private TextView nicknameView;
    private TextView emailView;
    private CircleImageView imageView;
    private User user;

    public ChatMemberItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_member_item, this, true);
        nicknameView = view.findViewById(R.id.nickname);
        emailView = view.findViewById(R.id.email);
        imageView = view.findViewById(R.id.image);
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getImg() {
        return img;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        setNickname(user.getNickname());
        setEmail(user.getEmail());
        setImg(user.getPicture());
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        nicknameView.setText(nickname);
    }

    public void setEmail(String email) {
        this.email = email;
        emailView.setText(email);
    }

    public void setImg(String img) {
        this.img = img;
    }

    public TextView getNicknameView() {
        return nicknameView;
    }

    public TextView getEmailView() {
        return emailView;
    }

    public CircleImageView getImageView() {
        return imageView;
    }
}
