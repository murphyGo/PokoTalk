package com.murphy.pokotalk;

public class Constants {
    public static final String serverURL = "https://192.168.0.2:4000";    /* Server Address */
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /* Location for saved session and application data */
    public static final String rootDirectory = "PokoTalk";   // Root directory
    public static final String sessionFile = "session.dat"; // Session info
    public static final String contactFile = "contact.dat";
    public static final String groupFile = "group.dat";
    public static final String eventFile = "event.dat";
    public static final String imagesDirectory = "img";
    public static final String attchedFileDirectory = "attach";


    /* Event names
    * event names must not conflict to Socket's predefined event names
    * (etc. EVENT_CONNECT...)*/

    /* Session event names */
    public static final String accountRegisteredName = "registerAccount";
    public static final String passwordLoginName = "passwordLogin";
    public static final String sessionLoginName = "sessionLogin";
    public static final String logoutName = "logout";

    /* Contact event names */
    public static final String getContactListName = "getContactList";
    public static final String getPendingContactListName = "getPendingContactList";
    public static final String addContactName = "addContact";
    public static final String removeContactName = "removeContact";
    public static final String acceptContactName = "acceptContact";
    public static final String denyContactName = "denyContact";
    public static final String newContactName = "newContact";
    public static final String newPendingContactName = "newPendingContact";
    public static final String contactDeniedName = "contactDenied";
    public static final String contactRemovedName = "contactRemoved";

    /* Group event names */
    public static final String getGroupListName = "getGroupList";
    public static final String addGroupName = "addGroup";
    public static final String contactChatRemovedName = "contactChatRemoved";
    public static final String membersInvitedName = "membersInvited";
    public static final String inviteGroupMembersName = "inviteGroupMembers";
    public static final String membersExitName = "membersExit";
    public static final String exitGroupName = "exitGroup";

    /* Chat event names */
    public static final String joinContactChatName = "joinContactChat";
    public static final String readMessageName = "readMessage";
    public static final String sendMessageName = "sendMessage";
    public static final String ackMessageName = "ackMessage";
    public static final String joinChatName = "joinChat";
    public static final String leaveChatName = "leaveChat";
    public static final String newMessageName = "newMessage";
    public static final String messageAckName = "messageAck";

    /* Event event names */
    public static final String getEventListName = "getEventList";
    public static final String createEventName = "createEvent";
    public static final String eventCreatedName = "eventCreated";
    public static final String eventExitName = "eventExit";
    public static final String eventParticipantExitedName = "eventParticipantExited";
    public static final String eventAckName = "eventAck";
    public static final String eventStartedName = "eventStarted";

    /* Event exists but not implemented yet by policy */
    public static final String membersJoinName = "membersJoin";
    public static final String membersLeaveName = "membersLeave";

    /* Request codes for activity call */
    public enum RequestCode{
        LOGIN(0),
        GROUP_ADD(1);

        public final int value;
        RequestCode(int v) {
            this.value = v;
        }
    }
}
