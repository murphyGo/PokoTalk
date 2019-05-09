package com.murphy.pokotalk.data.db.deprecated;

import android.os.Environment;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    protected HashMap<Integer, MessageFile> messageFiles;

    public FileManager() {
        sessionFile = new SessionFile();
        contactListFile = new ContactListFile();
        invitedFile = new InvitedPendingContactListFile();
        invitingFile = new InvitingPendingContactListFile();
        strangerFile = new StrangerFile();
        contactGroupFile = new ContactGroupFile();
        groupListFile = new GroupListFile();
        messageFiles = new HashMap<>();
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

    public MessageFile getMessageFile(Group group) {
        if (group == null) {
            return null;
        }

        MessageFile messageFile =  messageFiles.get(group.getGroupId());
        if (messageFile == null) {
            messageFile = new MessageFile(group);
            messageFiles.put(group.getGroupId(), messageFile);
        }

        return messageFile;
    }

    public boolean loadLastMessages() {
        GroupList groupList = DataCollection.getInstance().getGroupList();
        ArrayList<Group> groups = groupList.getList();

        for (Group group : groups) {
            MessageFile messageFile = getMessageFile(group);
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
            saveMessagesForGroup(group);
        }

        return true;
    }

    public boolean saveMessagesForGroup(Group group) {
        if (group == null) {
            return false;
        }

        MessageFile messageFile = getMessageFile(group);
        MessageList messageList = group.getMessageList();
        PokoMessage lastReadMessage = messageList.getItemByKey(messageFile.getLastMessageId());
        if (lastReadMessage == null) {
            return false;
        }

        ArrayList<PokoMessage> pokoMessages = messageList.getList();
        int index = pokoMessages.indexOf(lastReadMessage);
        if (index < 0) {
            return false;
        }

        try {
            Log.v("POKO", "WRITE MESSAGE FOR GROUP " + group.getGroupId());
            Log.v("POKO", "Last file message " + messageFile.getLastMessageId());
            messageFile.openWriter(true);
            for (index = index + 1; index < pokoMessages.size(); index++) {
                try {
                    messageFile.saveItemProcess(pokoMessages.get(index));
                    Log.v("POKO", "Write message " + pokoMessages.get(index).getMessageId());
                    Log.v("POKO", "Last file message " + messageFile.getLastMessageId());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            messageFile.closeWriter();
        } catch (IOException e) {
            e.printStackTrace();
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
            file.openWriter(false);
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
