package com.murphy.pokotalk.data.extra;

/** Date change item */
public class DateChangeItem<K> extends ExtraItem<K> {
    protected String dateChangeMessage;

    public String getDateChangeMessage() {
        return dateChangeMessage;
    }

    public void setDateChangeMessage(String dateChangeMessage) {
        this.dateChangeMessage = dateChangeMessage;
    }
}
