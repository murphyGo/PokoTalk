package com.murphy.pokotalk.data;

import java.util.Calendar;

public class Message {
    private int messageId;
    private Contact writer;
    private int messageType;
    private String content;
    private Calendar date;
    private int nbNotRead;

    /* Message type constants */
    public static final int MESSAGE = 0;
    public static final int IMAGE = 1;
    public static final int FILESHARE = 2;



    /* Getter and Setters */
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Contact getWriter() {
        return writer;
    }

    public void setWriter(Contact writer) {
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

    public int getNbNotRead() {
        return nbNotRead;
    }

    public void setNbNotRead(int nbNotRead) {
        this.nbNotRead = nbNotRead;
    }
}
