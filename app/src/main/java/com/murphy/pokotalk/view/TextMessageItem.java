package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class TextMessageItem extends MessageItem {
    private String nickname;
    private String img;
    private String content;
    private int nbNotReadUser;
    private Calendar time;
    private LinearLayout messageLayout;
    private TextView nicknameView;
    private CircleImageView imageView;
    private TextView messageView;
    private TextView nbNotReadUserView;
    private TextView timeView;

    public TextMessageItem(Context context) {
        super(context);
    }

    @Override
    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.message_item, this, true);
        messageLayout = view.findViewById(R.id.messageLayout);
        nicknameView = view.findViewById(R.id.userName);
        imageView = view.findViewById(R.id.userImage);
        messageView = view.findViewById(R.id.message);
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
            imageView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) timeView.getLayoutParams();
            params.addRule(RelativeLayout.END_OF, R.id.message);
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.message);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, -1);
        } else {
            messageLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            nicknameView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            nicknameView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
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
        nbNotReadUserView.setText(Integer.toString(nbNotReadUser));

        /* Number will not be shown if all user has seen the message */
        if (nbNotReadUser <= 0) {
            nbNotReadUserView.setVisibility(View.GONE);
        } else {
            nbNotReadUserView.setVisibility(View.VISIBLE);
        }
    }

    public TextView getMessageView() {
        return messageView;
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
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        simpleDateFormat.setTimeZone(Constants.timeZone);
        return simpleDateFormat.format(calendar.getTime());
    }
}

