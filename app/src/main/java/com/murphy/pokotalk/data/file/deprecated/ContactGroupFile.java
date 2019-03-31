package com.murphy.pokotalk.data.file.deprecated;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.user.ContactPokoList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ContactGroupFile extends PokoSequencialAccessFile<ContactPokoList.ContactGroupRelation> {
    protected ContactPokoList contactList;
    protected ArrayList<ContactPokoList.ContactGroupRelation> contactGroupRelations;

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
    public void addItem(ContactPokoList.ContactGroupRelation item) {
        contactList.putContactGroupRelation(item.getContactUserId(), item.getGroupId());
    }

    @Override
    public ContactPokoList.ContactGroupRelation getItemAt(int position) {
        return contactGroupRelations.get(position);
    }

    @Override
    public void saveItem(ContactPokoList.ContactGroupRelation item) throws IOException, JSONException {
        JSONObject jsonContact = Serializer.makeContactGroupRelationJSON(item);
        outputStreamWriter.write(jsonContact.toString());
    }

    @Override
    public ContactPokoList.ContactGroupRelation read() throws IOException, JSONException {
        JSONObject jsonContactGroupRelation = readJSON();
        if (jsonContactGroupRelation == null)
            return null;

        return Parser.parseContactGroupRelation(jsonContactGroupRelation);
    }
}
