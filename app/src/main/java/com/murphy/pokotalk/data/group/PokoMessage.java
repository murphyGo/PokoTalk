package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.user.User;

import java.util.Calendar;

public class PokoMessage extends Item {
    private int messageId;
    private int sendId;
    private User writer;
    private int messageType;
    private int importanceLevel;
    private String content;
    private String specialContent;
    private Calendar date;
    private int nbNotReadUser;    // number of users has not read this message
    private boolean acked;        // the user acked this message?

    /* PokoMessage type constants */
    public static final int TEXT_MESSAGE = 0;
    public static final int MEMBER_JOIN = 1;
    public static final int MEMBER_EXIT = 2;
    public static final int IMAGE = 3;
    public static final int FILESHARE = 4;
    public static final int APP_DATE_MESSAGE = 1000;

    /* PokoMessage importance level */
    public static final int NORMAL = 0;
    public static final int IMPORTANT = 1;
    public static final int VERY_IMPORTANT = 2;

    public PokoMessage() {
        setAcked(false);
    }

    public void update(Item item) {
        PokoMessage message = (PokoMessage) item;
        setContent(message.getContent());
        setDate(message.getDate());
        setMessageType(message.getMessageType());
        setNbNotReadUser(message.getNbNotReadUser());
    }

    /* Getter and Setters */
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public User getWriter() {
        return writer;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public int getNbNotReadUser() {
        return nbNotReadUser;
    }

    public void setNbNotReadUser(int nbNotRead) {
        this.nbNotReadUser = nbNotRead;
    }

    public int getSendId() {
        return sendId;
    }

    public void setSendId(int sendId) {
        this.sendId = sendId;
    }

    public int getImportanceLevel() {
        return importanceLevel;
    }

    public void setImportanceLevel(int importanceLevel) {
        this.importanceLevel = importanceLevel;
    }

    public void decrementNbNotReadUser() {
        if (--nbNotReadUser < 0)
            nbNotReadUser = 0;
    }

    public boolean isAcked() {
        return acked;
    }

    public void setAcked(boolean acked) {
        this.acked = acked;
    }

    public boolean isMyMessage(Session session) {
        return session.getUser().getUserId() == writer.getUserId();
    }

    public String getSpecialContent() {
        return specialContent;
    }

    public void setSpecialContent(String specialContent) {
        this.specialContent = specialContent;
    }
}
