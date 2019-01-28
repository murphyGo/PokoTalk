package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.user.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageItem extends FrameLayout {
    private String nickname;
    private String img;
    private String content;
    private int nbNotReadUser;
    private Context context;
    private TextView nicknameView;
    private CircleImageView imageView;
    private TextView messageView;
    private TextView nbNotReadUserView;
    private Message message;

    public MessageItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.message_item, this, true);
        nicknameView = view.findViewById(R.id.userName);
        imageView = view.findViewById(R.id.userImage);
        messageView = view.findViewById(R.id.message);
        nbNotReadUserView = view.findViewById(R.id.nbNotReadUser);
    }

    public String getNickname() {
        return nickname;
    }

    public String getImg() {
        return img;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
        User writer = message.getWriter();
        setNickname(writer.getNickname());
        setImg(writer.getPicture());
        setContent(message.getContent());
        setNbNotReadUser(message.getNbNotReadUser());
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        nicknameView.setText(nickname);
    }

    public void setImg(String img) {
        this.img = img;
    }

    public TextView getNicknameView() {
        return nicknameView;
    }

    public CircleImageView getImageView() {
        return imageView;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        messageView.setText(content);
    }

    public TextView getNbNotReadUser() {
        return nbNotReadUserView;
    }

    public void setNbNotReadUser(int nbNotReadUser) {
        this.nbNotReadUser = nbNotReadUser;
        nbNotReadUserView.setText(nbNotReadUser);
    }

    public TextView getMessageView() {
        return messageView;
    }

    public TextView getNbNotReadUserView() {
        return nbNotReadUserView;
    }
}

