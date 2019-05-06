package com.murphy.pokotalk.data.db.deprecated;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.db.json.Serializer;
import com.murphy.pokotalk.data.user.ContactList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ContactGroupFile extends PokoSequencialAccessFile<ContactList.ContactGroupRelation> {
    protected ContactList contactList;
    protected ArrayList<ContactList.ContactGroupRelation> contactGroupRelations;

    public ContactGroupFile() {
        super();
        contactList = DataCollection.getInstance().getContactList();
        contactGroupRelations = contactList.getContactGroupRelations();
    }

    @Override
    public String getFileName() {
        return Constants.contactGroupFile;
    }

    @Override
    public int getItemListSize() {
        return contactGroupRelations.size();
    }

    @Override
    public void addItem(ContactList.ContactGroupRelation item) {
        contactList.putContactGroupRelation(item.getContactUserId(), item.getGroupId());
    }

    @Override
    public ContactList.ContactGroupRelation getItemAt(int position) {
        return contactGroupRelations.get(position);
    }

    @Override
    public void saveItem(ContactList.ContactGroupRelation item) throws IOException, JSONException {
        JSONObject jsonContact = Serializer.makeContactGroupRelationJSON(item);
        outputStreamWriter.write(jsonContact.toString());
    }

    @Override
    public ContactList.ContactGroupRelation read() throws IOException, JSONException {
        JSONObject jsonContactGroupRelation = readJSON();
        if (jsonContactGroupRelation == null)
            return null;

        return Parser.parseContactGroupRelation(jsonContactGroupRelation);
    }
}
