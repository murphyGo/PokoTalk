package com.murphy.pokotalk;

public class Constants {
    public static final String serverURL = "https://192.168.0.2:4000";    /* Server Address */

    /* Event names
    * event names must not conflict to Socket's predefined event names
    * (etc. EVENT_CONNECT...)*/
    public static final String accountRegisteredName = "registerAccount";
    public static final String passwordLoginName = "passwordLogin";
    public static final String sessionLoginNam = "sessionLogin";
}
