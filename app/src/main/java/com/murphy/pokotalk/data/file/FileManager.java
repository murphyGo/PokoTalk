package com.murphy.pokotalk.data.file;

import android.os.Environment;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.contact.ContactGroupFile;
import com.murphy.pokotalk.data.file.contact.ContactListFile;
import com.murphy.pokotalk.data.file.contact.InvitedPendingContactListFile;
import com.murphy.pokotalk.data.file.contact.InvitingPendingContactListFile;
import com.murphy.pokotalk.data.file.contact.StrangerFile;
import com.murphy.pokotalk.data.file.group.GroupListFile;
import com.murphy.pokotalk.data.file.group.MessageFile;
import com.murphy.pokotalk.data.file.session.SessionFile;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/* Manages saving data as a file, reading from and writing to files */
public class FileManager {
    protected static FileManager instance;
    protected static String rootPath = null;
    protected SessionFile sessionFile;
    protected ContactListFile contactListFile;
    protected InvitedPendingContactListFile invitedFile;
    protected InvitingPendingContactListFile invitingFile;
    protected StrangerFile strangerFile;
    protected ContactGroupFile contactGroupFile;
    protected GroupListFile groupListFile;

    public FileManager() {
        sessionFile = new SessionFile();
        contactListFile = new ContactListFile();
        invitedFile = new InvitedPendingContactListFile();
        invitingFile = new InvitingPendingContactListFile();
        strangerFile = new StrangerFile();
        contactGroupFile = new ContactGroupFile();
        groupListFile = new GroupListFile();
    }

    public static FileManager getInstance() {
        if (instance == null)
            instance = new FileManager();

        return instance;
    }

    public static String getRootPath() {
        if (rootPath == null) {
            File sdcardFolder = Environment.getExternalStorageDirectory();
            String sdcardPath = sdcardFolder.getAbsolutePath();
            rootPath = sdcardPath + File.separator + Constants.rootDirectory;
        }

        return rootPath;
    }

    public void makeSureRootDirectoryExists() throws FileNotFoundException, IOException {
        /* Make sure the root directory exists */
        File directory = new File(getRootPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public boolean loadSession() {
        return readFromFile(sessionFile);
    }

    public boolean loadContactList() {
        return readAllFromFile(contactListFile);
    }

    public boolean loadPendingContactList() {
        boolean invited = readAllFromFile(invitedFile);
        boolean inviting = readAllFromFile(invitingFile);
        return invited && inviting;
    }

    public boolean loadStragerList() {
        return readAllFromFile(strangerFile);
    }

    public boolean loadContactGroupRelations() {
        return readAllFromFile(contactGroupFile);
    }

    public boolean loadGroupList() {
        return readAllFromFile(groupListFile);
    }

    public boolean loadLastMessages() {
        GroupList groupList = DataCollection.getInstance().getGroupList();
        ArrayList<Group> groups = groupList.getList();

        for (Group group : groups) {
            MessageFile messageFile = new MessageFile(group);
            try {
                messageFile.openReader();
                messageFile.readNextLatestMessages(1);
                messageFile.closeReader();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public boolean saveSession() {
        return saveToFile(sessionFile);
    }

    public boolean saveContactList() {
        return saveToFile(contactListFile);
    }

    public boolean savePendingContactList() {
        boolean invited = saveToFile(invitedFile);
        boolean inviting = saveToFile(invitingFile);

        return invited && inviting;
    }

    public boolean saveStrangerList() {
        return saveToFile(strangerFile);
    }

    public boolean saveContactGroupRelations() {
        return saveToFile(contactGroupFile);
    }

    public boolean saveGroupList() {
        return saveToFile(groupListFile);
    }


    public boolean saveMessages() {
        GroupList groupList = DataCollection.getInstance().getGroupList();
        ArrayList<Group> groups = groupList.getList();

        for (Group group : groups) {
            MessageFile messageFile = new MessageFile(group);
            saveToFile(messageFile);
        }

        return true;
    }

    protected boolean readFromFile(PokoSequencialAccessFile file) {
        try {
            file.openReader();
            file.read();
            file.closeReader();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean readAllFromFile(PokoSequencialAccessFile file) {
        try {
            file.openReader();
            file.readAll();
            file.closeReader();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean saveToFile(PokoSequencialAccessFile file) {
        try {
            file.openWriter();
            file.save();
            file.flush();
            file.closeWriter();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public SessionFile getSessionFile() {
        return sessionFile;
    }

    public ContactListFile getContactListFile() {
        return contactListFile;
    }

    public InvitedPendingContactListFile getInvitedFile() {
        return invitedFile;
    }

    public InvitingPendingContactListFile getInvitingFile() {
        return invitingFile;
    }

    public StrangerFile getStrangerFile() {
        return strangerFile;
    }

    public ContactGroupFile getContactGroupFile() {
        return contactGroupFile;
    }

    public GroupListFile getGroupListFile() {
        return groupListFile;
    }
}
