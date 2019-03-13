package com.murphy.pokotalk.data.file.deprecated;

import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public abstract class PendingContactListFile extends PokoSequencialAccessFile<PendingContact> {
    protected PendingContactList pendingContactList;

    public PendingContactListFile() {
        super();
        pendingContactList = getPendingContactList();
    }

    public abstract PendingContactList getPendingContactList();

    @Override
    public int getItemListSize() {
        return pendingContactList.getList().size();
    }

    @Override
    public void addItem(PendingContact item) {
        pendingContactList.updateItem(item);
    }

    @Override
    public PendingContact getItemAt(int position) {
        return pendingContactList.getList().get(position);
    }

    @Override
    public void saveItem(PendingContact item) throws IOException, JSONException {
        JSONObject jsonContact = Serializer.makePendingContactJSON(item);
        outputStreamWriter.write(jsonContact.toString());
    }

    @Override
    public PendingContact read() throws IOException, JSONException {
        JSONObject jsonPendingContact = readJSON();
        if (jsonPendingContact == null)
            return null;

        return Parser.parsePendingContact(jsonPendingContact);
    }

}
