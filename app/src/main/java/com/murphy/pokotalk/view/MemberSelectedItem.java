package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.content.ContentManager;
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
    protected ContentManager.ImageContentLoadCallback userImageLocateCallback;

    public MemberSelectedItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.member_selected_item, this, true);
        nicknameView = view.findViewById(R.id.nickname);
        imageView = view.findViewById(R.id.image);
        removeButton = view.findViewById(R.id.removeButton);
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

        // Cancel user image locate callback
        if (userImageLocateCallback != null) {
            userImageLocateCallback.cancel();
            userImageLocateCallback = null;
        }

        if (img != null) {
            if (img == "null") {
                Log.e("POKO", "BAD, image of name string null");
            }

            userImageLocateCallback = new ContentManager.ImageContentLoadCallback() {
                @Override
                public void onError() {
                    imageView.setImageResource(R.drawable.user);
                }

                @Override
                public void onLoadImage(final Bitmap image) {
                    imageView.setImageBitmap(image);
                }
            };

            // Locate image
            ContentManager.getInstance()
                    .locateThumbnailImage(context, img, userImageLocateCallback);
        } else {
            // Set default image
            imageView.setImageResource(R.drawable.user);
        }
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
