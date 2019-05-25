package com.murphy.pokotalk;

import java.util.Locale;
import java.util.TimeZone;

public class Constants {
    public static final String[] serverURLs = {"https://192.168.0.2",
            "https://172.30.1.6", "https://221.155.4.132", "https://220.67.71.90"};
    public static final int serverPort = 4000;
    public static final String serverURL = serverURLs[0] + ":" + serverPort;    /* Server Address */
    public static final String serverDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final Locale locale = Locale.KOREA;
    public static final TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");

    /* Chat constants */
    public static final String chatDateChangeFormat = "yyyy년 M월 d일";

    /* Content location */
    public static final String pokoTalkContentDirectory = "PokoTalk";

    /* File upload size limit in bytes */
    public static final int uploadFileSizeLimit = 1024 * 1024 * 100;    // 100MB

    /* Location for saved session and application data */
    public static final String fileEncoding = "UTF8";
    public static final String rootDirectory = "PokoTalk";   // Root directory
    public static final String sessionFile = "session.dat";  // Session info
    public static final String contactFile = "contact.dat";
    public static final String contactGroupFile = "contactGroup.dat";
    public static final String invitedPendingContactFile = "invited.dat";
    public static final String invitingPendingContactFile = "inviting.dat";
    public static final String strangerFile = "stranger.dat";
    public static final String groupFile = "group.dat";
    public static final String messageFileFormat = "groupMsg%d.dat";
    public static final String eventFile = "event.dat";
    public static final String messageDirectory = "msg";
    public static final String imagesDirectory = "img";
    public static final String attchedFileDirectory = "attach";

    /* Permission request code */
    public static final int ALL_PERMISSION = 100;
    public static final int READ_EXTERNAL_STORAGE = 0;
    public static final int WRITE_EXTERNAL_STORAGE = 1;
    public static final int CAMERA_PERMISSION = 2;
    public static final int LOCATION_PERMISSION = 3;

    /* PokoEvent names
    * event names must not conflict to Socket's predefined event names
    * (etc. EVENT_CONNECT...)*/

    /* Session event names */
    public static final String accountRegisteredName = "registerAccount";
    public static final String passwordLoginName = "passwordLogin";
    public static final String sessionLoginName = "sessionLogin";
    public static final String logoutName = "logoutState";

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
    public static final String readNbreadOfMessages = "readNbreadOfMessages";
    public static final String sendMessageName = "sendMessage";
    public static final String sendImageMessageName = "sendImageMessage";
    public static final String sendFileShareMessageName = "sendFileShareMessage";
    public static final String ackMessageName = "ackMessage";
    public static final String joinChatName = "joinChat";
    public static final String leaveChatName = "leaveChat";
    public static final String newMessageName = "newMessage";
    public static final String messageAckName = "messageAck";
    public static final String getMemberJoinHistory = "getMemberJoinHistory";

    /* PokoEvent event names */
    public static final String getEventListName = "getEventList";
    public static final String createEventName = "createEvent";
    public static final String eventCreatedName = "eventCreated";
    public static final String eventExitName = "eventExit";
    public static final String eventParticipantExitedName = "eventParticipantExited";
    public static final String eventAckName = "eventAck";
    public static final String eventStartedName = "eventStarted";

    /* PokoEvent exists but not implemented yet by policy */
    public static final String membersJoinName = "membersJoin";
    public static final String membersLeaveName = "membersLeave";

    /* Upload and download contents */
    public static final String startUploadName = "startUpload";
    public static final String uploadName = "upload";
    public static final String uploadAckName = "uploadAck";
    public static final String startDownloadName = "startDownload";
    public static final String downloadName = "download";
    public static final String downloadAckName = "downloadAck";

    /* Location share */
    public static final String joinRealtimeLocationShareName = "joinRealtimeLocationShare";
    public static final String exitRealtimeLocationShareName = "exitRealtimeLocationShare";
    public static final String updateRealtimeLocationName = "updateRealtimeLocation";
    public static final String realtimeLocationShareBroadcastName = "realtimeLocationShareBroadcast";

    /* Settings events */
    public static final String updateProfileImageName = "updateProfileImage";

    /* Chat constants */
    public static final int nbMessageRead = 100;   // Maximum number of messages for chat
    public static final String unknownUserNickname = "알 수 없는 유저";
    public static final String unknownUserEmail = "???";
    public static final String unknownEventName = "???";

    /* Request codes for activity call */
    public enum RequestCode {
        LOGIN(0),
        GROUP_ADD(1),
        GROUP_CHAT(2),
        EVENT_CREATE(3),
        PROFILE_UPDATE(4),
        PROFILE_GALLERY(5),
        PROFILE_CAMERA(6),
        IMAGE_EDITION(7),
        ATTACH_IMAGE(8),
        ATTACH_CAMERA_PICTURE(9),
        ATTACH_FILE(10),
        EVENT_DETAIL(11),
        LOCATION_SETTING(12),
        TAKE_PICTURE(13);

        public final int value;
        RequestCode(int v) {
            this.value = v;
        }
    }
}
