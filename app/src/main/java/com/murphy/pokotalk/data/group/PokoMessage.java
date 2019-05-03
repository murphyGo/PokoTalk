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

    /* PokoMessage type constants */
    public static final int TYPE_TEXT_MESSAGE = 0;
    public static final int TYPE_MEMBER_JOIN = 1;
    public static final int TYPE_MEMBER_EXIT = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_FILE_SHARE = 4;
    public static final int TYPE_APP_DATE_MESSAGE = 1000;

    /* PokoMessage importance level */
    public static final int IMPORTANCE_NORMAL = 0;
    public static final int IMPORTANCE_IMPORTANT = 1;
    public static final int IMPORTANCE_VERY_IMPORTANT = 2;

    public PokoMessage() {

    }

    public void update(Item item) {
        PokoMessage message = (PokoMessage) item;
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
        if (--nbNotReadUser < 0) {
            nbNotReadUser = 0;
        }
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

    public String getRealContent() {
        switch (getMessageType()) {
            case TYPE_TEXT_MESSAGE: {
                return getContent();
            }
            case TYPE_MEMBER_JOIN: {
                String content = getSpecialContent();
                if (content == null) {
                    return getWriter().getNickname() + " 님이 초대하셨습니다.";
                } else {
                    return content;
                }
            }
            case TYPE_MEMBER_EXIT: {
                return getSpecialContent();
            }
            case TYPE_IMAGE: {
                return "사진";
            }
            case TYPE_FILE_SHARE: {
                return "파일";
            }
            default: {
                return "";
            }
        }
    }
}
