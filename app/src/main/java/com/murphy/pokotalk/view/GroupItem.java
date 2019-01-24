package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.group.Group;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupItem extends FrameLayout {
    private String groupName;
    private String alias;
    private String lastMessage;
    private int nbMembers;
    private int nbNewMessage;
    private Calendar lastMessageDate;
    private Context context;
    private TextView groupNameView;
    private TextView lastMessageView;
    private TextView nbMemberView;
    private TextView nbNewMessageView;
    private TextView lastMessageDateView;
    private ImageView imageView;
    private Group group;

    public GroupItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.group_item, this, true);
        groupNameView = (TextView) view.findViewById(R.id.groupName);
        lastMessageView = (TextView) view.findViewById(R.id.lastMessage);
        nbNewMessageView = (TextView) view.findViewById(R.id.nbNewMessage);
        nbMemberView = (TextView) view.findViewById(R.id.nbMember);
        lastMessageView = (TextView) view.findViewById(R.id.lastMessageDate);
        imageView = (CircleImageView) view.findViewById(R.id.image);
    }

    /** Get last message date string from calendar */
    public String getLastMessageDateString(Calendar calendar) {
        Calendar now = Calendar.getInstance();
        long diffSec = (now.getTime().getTime() - calendar.getTime().getTime())/1000;

        if (diffSec < 10) {
            return "방금 전";
        }

        if (diffSec < 60)
            return diffSec + "초 전";

        diffSec /= 60;
        if (diffSec < 60)
            return diffSec + "분 전";
        diffSec /= 24;
        if (diffSec < 24)
            return diffSec + "시간 전";
        diffSec /= 365;
        if (diffSec < 2)
            return "어제";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(calendar.getTime());
    }

    /** Set group and update view contents */
    public void setGroup(Group group) {
        this.group = group;
        setGroupName(group.getGroupName());
        if (group.getAlias() != null)
            setGroupName(group.getAlias());
        setNbMembers(group.getMembers().getList().size());
        setNbNewMessage(group.getNbNewMessages());
        //TODO: last message and last message date
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
        groupNameView.setText(groupName);
    }

    public void setAlias(String alias) {
        this.alias = alias;
        groupNameView.setText(alias);
    }

    public void setNbNewMessage(int nbNewMessage) {
        this.nbNewMessage = nbNewMessage;
        if (nbNewMessage <= 0) {
            nbNewMessageView.setVisibility(View.INVISIBLE);
        } else {
            nbNewMessageView.setVisibility(View.VISIBLE);
            nbNewMessageView.setText(nbNewMessage);
        }
    }

    public void setNbMembers(int nbMembers) {
        this.nbMembers = nbMembers;
        if (nbMembers < 3) {
            nbMemberView.setVisibility(View.INVISIBLE);
        } else {
            nbMemberView.setVisibility(View.VISIBLE);
            nbMemberView.setText("(" + nbMembers + "명)");
        }
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        lastMessageView.setText(lastMessage);
    }

    public void setLastMessageDate(Calendar lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
        lastMessageDateView.setText(getLastMessageDateString(lastMessageDate));
    }

    public String getGroupName() {
        return groupName;
    }

    public String getAlias() {
        return alias;
    }


    public String getLastMessage() {
        return lastMessage;
    }


    public int getNbMembers() {
        return nbMembers;
    }


    public int getNbNewMessage() {
        return nbNewMessage;
    }

    public Calendar getLastMessageDate() {
        return lastMessageDate;
    }

    public Group getGroup() {
        return group;
    }

    public TextView getGroupNameView() {
        return groupNameView;
    }

    public TextView getLastMessageView() {
        return lastMessageView;
    }

    public TextView getNbNewMessageView() {
        return nbNewMessageView;
    }

    public TextView getLastMessageDateView() {
        return lastMessageDateView;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
