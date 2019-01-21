package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.murphy.pokotalk.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class InvitedContactItem extends FrameLayout {
    private String nickname;
    private String email;
    private String img;
    private Context context;
    private TextView nicknameView;
    private TextView emailView;
    private ImageView imageView;

    public InvitedContactItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.contact_item, this, true);
        nicknameView = (TextView) view.findViewById(R.id.nickname);
        emailView = (TextView) view.findViewById(R.id.email);
        imageView = (CircleImageView) view.findViewById(R.id.image);
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
