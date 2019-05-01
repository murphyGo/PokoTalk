package com.murphy.pokotalk.data;

import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactPokoList;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.StrangerPokoList;
import com.murphy.pokotalk.data.user.User;

public class DataCollection {
    private ContactPokoList contactList;
    private PendingContactPokoList invitedContactList;
    private PendingContactPokoList invitingContactList;
    private StrangerPokoList strangerList;
    private GroupPokoList groupList;
    private EventList eventList;
    private static DataCollection instance;

    public DataCollection() {
        contactList = new ContactPokoList();
        invitedContactList = new PendingContactPokoList();
        invitingContactList = new PendingContactPokoList();
        strangerList = new StrangerPokoList();
        groupList = new GroupPokoList();
        eventList = new EventList();
    }

    public static DataCollection getInstance() {
        if (instance == null) {
            synchronized (DataCollection.class) {
                instance = instance == null ? new DataCollection() : instance;
            }
        }

        return instance;
    }

    // Reset application data. It simply discard the instance.
    public static void reset() {
        instance = null;
    }

    /** Insert or update user on proper user list depending on user type
     * @param user
     * @return true if user is successfully inserted or updated, false otherwise.
     */
    public boolean updateUserList(User user) {
        ContactPokoList contactList = getContactList();
        PendingContactPokoList invitedContactList = getInvitedContactList();
        PendingContactPokoList invitingContactList = getInvitingContactList();
        StrangerPokoList strangerList = getStrangerList();

        User u = Session.getInstance().getUser();
        if (u != null) {
            if (u.getUserId() == user.getUserId()) {
                u.update(user);
                return true;
            }
        }

        if (user instanceof Contact) {
            contactList.updateItem((Contact) user);
        } else if (user instanceof PendingContact &&
                invitedContactList.getItemByKey(invitedContactList.
                getKey((PendingContact) user)) != null) {
            invitedContactList.updateItem((PendingContact) user);
        } else if (user instanceof PendingContact &&
                invitingContactList.getItemByKey(invitingContactList.
                        getKey((PendingContact) user)) != null) {
            invitingContactList.updateItem((PendingContact) user);
        } else if (user instanceof Stranger) {
            strangerList.updateItem((Stranger) user);
        } else {
            return false;
        }

        return true;
    }

    /** Find user with userId in DataCollection and returns User if exists.
     * returns null if not found.
     * @param userId
     * @return User
     */
    public User getUserById(int userId) {
        User result;

        User user = Session.getInstance().getUser();
        if (user != null) {
            if (user.getUserId() == userId) {
                return user;
            }
        }

        if ((result = getContactList().getItemByKey(userId)) != null)
            return result;
        else if ((result = getInvitedContactList().getItemByKey(userId)) != null)
            return result;
        else if ((result = getInvitingContactList().getItemByKey(userId)) != null)
            return result;
        else if ((result = getStrangerList().getItemByKey(userId)) != null)
            return result;

        return null;
    }

    /** Removes user with id from list and move to StrangerPokoList
     * If the user is Stranger or session user, do nothing.
     * @param userId
     * @return Stranger user moved to StrangerPokoList
     */
    public Stranger moveUserToStrangerList(int userId) {
        String email, nickname, picture;
        ContactPokoList contactList = getContactList();
        PendingContactPokoList invitedContactList = getInvitedContactList();
        PendingContactPokoList invitingContactList = getInvitingContactList();
        StrangerPokoList strangerList = getStrangerList();

        User user = Session.getInstance().getUser();
        if (user != null) {
            if (user.getUserId() == userId) {
                return null;
            }
        }

        /* If the user is Stranger, do nothing */
        Stranger stranger = strangerList.getItemByKey(userId);
        if (stranger != null)
            return stranger;

        /* Remove contact from lists */
        /* When the user is in Stranger list, don't have to remove */
        Contact contact = contactList.removeItemByKey(userId);
        PendingContact pendingContact = invitedContactList.removeItemByKey(userId);
        PendingContact pendingContact2 = invitingContactList.removeItemByKey(userId);
        if (contact != null) {
            email = contact.getEmail();
            nickname = contact.getNickname();
            picture = contact.getPicture();
        } else if (pendingContact != null) {
            email = pendingContact.getEmail();
            nickname = pendingContact.getNickname();
            picture = pendingContact.getPicture();
        } else if (pendingContact2 != null) {
            email = pendingContact2.getEmail();
            nickname = pendingContact2.getNickname();
            picture = pendingContact2.getPicture();
        } else {
            return null;
        }

        /* The user now becomes stranger */
        stranger = new Stranger();
        stranger.setUserId(userId);
        stranger.setEmail(email);
        stranger.setNickname(nickname);
        stranger.setPicture(picture);
        strangerList.updateItem(stranger);

        return stranger;
    }

    public int getTotalNewMessageNumber() {
        int result = 0;
        GroupPokoList groupList = getGroupList();

        for (Group group : groupList.getList()) {
            result += group.getNbNewMessages();
        }

        return result;
    }

    /* Getter methods */
    public ContactPokoList getContactList() {
        return contactList;
    }

    public PendingContactPokoList getInvitedContactList() {
        return invitedContactList;
    }

    public PendingContactPokoList getInvitingContactList() {
        return invitingContactList;
    }

    public StrangerPokoList getStrangerList() { return strangerList; }

    public GroupPokoList getGroupList() {
        return groupList;
    }

    public EventList getEventList() {
        return eventList;
    }
}
