package com.murphy.pokotalk.data.file.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoSequencialAccessFile;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ContactListFile extends PokoSequencialAccessFile<Contact> {
    protected ContactList contactList;

    public ContactListFile() {
        super();
        contactList = DataCollection.getInstance().getContactList();
    }

    @Override
    public String getFileName() {
        return Constants.contactFile;
    }

    @Override
    public int getItemListSize() {
        return contactList.getList().size();
    }

    @Override
    public void addItem(Contact item) {
        Log.v("add contact ", item.getNickname() + " " + item.getUserId());
        contactList.updateItem(item);
    }

    @Override
    public Contact getItemAt(int position) {
        return contactList.getList().get(position);
    }

    @Override
    public void saveItem(Contact item) throws IOException, JSONException {
        JSONObject jsonContact = Serializer.makeContactJSON(item);
        outputStreamWriter.write(jsonContact.toString());
    }

    @Override
    public Contact read() throws IOException, JSONException {
        JSONObject jsonContact = readJSON();
        if (jsonContact == null)
            return null;

        return Parser.parseContact(jsonContact);
    }

}
