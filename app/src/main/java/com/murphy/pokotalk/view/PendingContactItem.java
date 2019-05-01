package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.content.ContentManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class PendingContactItem extends FrameLayout {
    private String nickname;
    private String email;
    private String img;
    private Context context;
    private TextView nicknameView;
    private TextView emailView;
    private CircleImageView imageView;
    private Button acceptButton;
    private Boolean invited;
    private PendingContact pendingContact;

    public PendingContactItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pending_contact_item, this, true);
        nicknameView = (TextView) view.findViewById(R.id.nickname);
        emailView = (TextView) view.findViewById(R.id.email);
        imageView = (CircleImageView) view.findViewById(R.id.image);
        acceptButton = (Button) view.findViewById(R.id.acceptButton);
    }

    public void setPendingContact(PendingContact pendingContact) {
        this.pendingContact = pendingContact;
        setNickname(pendingContact.getNickname());
        setEmail(pendingContact.getEmail());
        setImg(pendingContact.getPicture());
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

        if (img != null) {
            if (img == "null") {
                Log.e("POKO", "BAD, image of name string null");
            }

            // Locate image
            ContentManager.getInstance().locateThumbnailImage(context, img,
                    new ContentManager.ImageContentLoadCallback() {
                        @Override
                        public void onError() {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageResource(R.drawable.user);
                                }
                            });
                        }

                        @Override
                        public void onLoadImage(final Bitmap image) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(image);
                                }
                            });
                        }
                    });
        } else {
            imageView.setImageResource(R.drawable.user);
        }
    }

    public Boolean getInvited() {
        return invited;
    }

    public PendingContact getPendingContact() {
        return pendingContact;
    }

    public void setInvited(Boolean invited) {
        this.invited = invited;

        if (invited)
            acceptButton.setVisibility(View.VISIBLE);
        else
            acceptButton.setVisibility(View.INVISIBLE);
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

    public Button getAcceptButton() {
        return acceptButton;
    }
}
