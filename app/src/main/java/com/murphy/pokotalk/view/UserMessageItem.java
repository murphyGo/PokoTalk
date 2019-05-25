package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
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
    protected ImageView messageImageView;
    protected TextView nbNotReadUserView;
    protected TextView timeView;
    protected RelativeLayout fileShareLayout;
    protected RelativeLayout afterPictureLayout;
    protected TextView fileNameView;
    protected ContentManager.ImageContentLoadCallback userImageLocateCallback;

    private static int parentWidthPixel = -1;
    private static float maxContentRatio = 0.75f;

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
        messageImageView = view.findViewById(R.id.messageImageView);
        nbNotReadUserView = view.findViewById(R.id.nbNotReadUser);
        timeView = view.findViewById(R.id.time);
        fileShareLayout = view.findViewById(R.id.messageFileShareLayout);
        fileNameView = view.findViewById(R.id.messageFileName);
        afterPictureLayout = view.findViewById(R.id.messageAfterPicture);

        // Measure content layout size
        if (parentWidthPixel < 0) {
            final ViewTreeObserver vto = afterPictureLayout.getViewTreeObserver();
            final ViewTreeObserver.OnGlobalLayoutListener layoutListener =
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    afterPictureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Get measured width
                    parentWidthPixel = afterPictureLayout.getMeasuredWidth();
                }
            };

            vto.addOnGlobalLayoutListener(layoutListener);
        }
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

    protected void adjustContentWidth() {
        // Adjust content width so that content does not overflow in horizontal
        int contentPixel = contentWrapperView.getWidth();

        int maxWithInPixel = (int) (parentWidthPixel * maxContentRatio);
        Log.v("POKO", "PARENT" + parentWidthPixel);
        Log.v("POKO", "MAX " + maxWithInPixel + " CUR, " + contentPixel);
        if (contentPixel > maxWithInPixel) {
            contentWrapperView.getLayoutParams().width = maxWithInPixel;
        }
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
            if (img.equals("null")) {
                Log.e("POKO", "BAD, image of name string null");
            }

            userImageLocateCallback = new ContentManager.ImageContentLoadCallback() {
                @Override
                public void onError() {
                    userImageView.setImageResource(R.drawable.user);
                }

                @Override
                public void onLoadImage(final Bitmap image) {
                    userImageView.setImageBitmap(image);
                }
            };

            // Locate image
            ContentManager.getInstance()
                    .locateThumbnailImage(context, img, userImageLocateCallback);
        } else {
            // Set default image
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

    public ImageView getMessageImageView() {
        return messageImageView;
    }
}
