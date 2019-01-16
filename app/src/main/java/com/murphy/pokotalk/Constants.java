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
    public static final String accountRegisteredName = "registerAccount";
    public static final String passwordLoginName = "passwordLogin";
    public static final String sessionLoginName = "sessionLogin";


    /* Request codes for activity call */
    enum RequestCode{
        LOGIN(0);

        public final int value;
        RequestCode(int v) {
            this.value = v;
        }
    }
}
