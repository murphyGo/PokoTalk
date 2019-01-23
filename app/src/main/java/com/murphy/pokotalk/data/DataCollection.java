package com.murphy.pokotalk.data;

import android.os.Environment;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.StrangerList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataCollection {
    private Session session;
    private String rootDirectory;
    private ContactList contactList;
    private PendingContactList invitedContactList;
    private PendingContactList invitingContactList;
    private StrangerList strangerList;
    private GroupList groupList;
    private EventList eventList;
    private static DataCollection instance;

    public DataCollection() {
        contactList = new ContactList();
        invitedContactList = new PendingContactList();
        invitingContactList = new PendingContactList();
        strangerList = new StrangerList();
        groupList = new GroupList();
        eventList = new EventList();
        session = Session.getInstance();
    }

    public static DataCollection getInstance() {
        if (instance == null)
            instance = new DataCollection();

        return instance;
    }

    public String getSDCardLocation() {
        File sdcardFolder = Environment.getExternalStorageDirectory();
        String sdcardPath = sdcardFolder.getAbsolutePath();
        return sdcardPath;
    }

    /* Load session data */
    public void loadSessionData() {
        String sessionId = null;
        rootDirectory = getSDCardLocation() + File.separator + Constants.rootDirectory;
        String sessionFileLocation = rootDirectory + File.separator + Constants.sessionFile;
        try{
            FileInputStream sessionFile = new FileInputStream(new File(sessionFileLocation));
            BufferedReader sessionReader = new BufferedReader(new InputStreamReader(sessionFile));
            try {
                String line;
                while ((line = sessionReader.readLine()) != null) {

                }
            } catch (IOException e) {

            }
        } catch (FileNotFoundException e) {

        }

        session.setSessionId(sessionId);
    }

    /* Load application data(user data, contacts, groups...) after session is decided */
    public void loadApplicationData() {

    }

    public static void reset() {
        instance = null;
    }


    /* Getter methods */
    public ContactList getContactList() {
        return contactList;
    }

    public PendingContactList getInvitedContactList() {
        return invitedContactList;
    }

    public PendingContactList getInvitingContactList() {
        return invitingContactList;
    }

    public StrangerList getStrangerList() { return strangerList; }

    public GroupList getGroupList() {
        return groupList;
    }

    public EventList getEventList() {
        return eventList;
    }
}
