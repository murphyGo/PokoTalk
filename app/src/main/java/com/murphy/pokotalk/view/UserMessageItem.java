package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.content.ContentManager;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class UserMessageItem extends MessageItem {
    protected String nickname;
    protected String img;
    protected String content;
    protected int nbNotReadUser;
    protected Calendar time;
    protected LinearLayout messageLayout;
    protected TextView nicknameView;
    protected CircleImageView userImageView;
    protected FrameLayout contentWrapperView;
    protected TextView textMessageView;
    protected ImageView messageImageVihew;
    protected TextView nbNotReadUserView;
    protected TextView timeView;

    public UserMessageItem(Context context) {
        super(context);
    }

    @Override
    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.message_item, this, true);
        messageLayout = view.findViewById(R.id.messageLayout);
        nicknameView = view.findViewById(R.id.userName);
        userImageView = view.findViewById(R.id.userImage);
        contentWrapperView = view.findViewById(R.id.messageContentWrapper);
        textMessageView = view.findViewById(R.id.messageContent);
        messageImageVihew = view.findViewById(R.id.messageImageView);
        nbNotReadUserView = view.findViewById(R.id.nbNotReadUser);
        timeView = view.findViewById(R.id.time);
    }

    public String getNickname() {
        return nickname;
    }

    public String getImg() {
        return img;
    }

    @Override
    public void setMessage(PokoMessage message) {
        this.message = message;
        User writer = message.getWriter();
        setNickname(writer.getNickname());
        setImg(writer.getPicture());
        setContent(message.getContent());
        setNbNotReadUser(message.getNbNotReadUser());
        setTime(message.getDate());

        Session session = Session.getInstance();
        /* If it is my message, use rtl direction, ltr otherwise */
        if (session.getUser().getUserId() == writer.getUserId()) {
            messageLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            nicknameView.setVisibility(View.GONE);
            userImageView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) timeView.getLayoutParams();
            params.addRule(RelativeLayout.END_OF, R.id.messageContentWrapper);
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.messageContentWrapper);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, -1);
        } else {
            messageLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            nicknameView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            nicknameView.setVisibility(View.VISIBLE);
            userImageView.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) timeView.getLayoutParams();
            params.addRule(RelativeLayout.END_OF, R.id.userName);
            params.addRule(RelativeLayout.ALIGN_TOP,-1);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.userName);
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        nicknameView.setText(nickname);
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
                                    userImageView.setImageResource(R.drawable.user);
                                }
                            });
                        }

                        @Override
                        public void onLoadImage(final Bitmap image) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    userImageView.setImageBitmap(image);
                                }
                            });
                        }
                    });
        } else {
            userImageView.setImageResource(R.drawable.user);
        }
    }

    public TextView getNicknameView() {
        return nicknameView;
    }

    public CircleImageView getUserImageView() {
        return userImageView;
    }

    public String getContent() {
        return content;
    }

    public abstract void setContent(String content);

    public TextView getNbNotReadUser() {
        return nbNotReadUserView;
    }

    public void setNbNotReadUser(int nbNotReadUser) {
        this.nbNotReadUser = nbNotReadUser;
        nbNotReadUserView.setText(Integer.toString(nbNotReadUser));

        /* Number will not be shown if all user has seen the message */
        if (nbNotReadUser <= 0) {
            nbNotReadUserView.setVisibility(View.GONE);
        } else {
            nbNotReadUserView.setVisibility(View.VISIBLE);
        }
    }

    public TextView getTextMessageView() {
        return textMessageView;
    }

    public TextView getNbNotReadUserView() {
        return nbNotReadUserView;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
        timeView.setText(calendarToString(time));
    }

    public String calendarToString(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(Constants.timeZone);
        return simpleDateFormat.format(calendar.getTime());
    }

    public ImageView getMessageImageVihew() {
        return messageImageVihew;
    }
}
