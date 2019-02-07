package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.user.Contact;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberSelectedItem extends FrameLayout {
    private String nickname;
    private String img;
    private Context context;
    private TextView nicknameView;
    private CircleImageView imageView;
    private Button removeButton;
    private Contact contact;

    public MemberSelectedItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.member_selected_item, this, true);
        nicknameView = (TextView) view.findViewById(R.id.nickname);
        imageView = (CircleImageView) view.findViewById(R.id.image);
        removeButton = (Button) view.findViewById(R.id.removeButton);
    }

    public String getNickname() {
        return nickname;
    }

    public String getImg() {
        return img;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
        setNickname(contact.getNickname());
        setImg(contact.getPicture());
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

    public Button getRemoveButton() {
        return removeButton;
    }
}
