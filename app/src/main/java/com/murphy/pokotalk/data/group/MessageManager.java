package com.murphy.pokotalk.data.group;

/** Message manager captures possibility of missing some messages in message list
 * and request those messages from server, which results in the user will EVENTUALLY
 * receive all messages without missing any one message.
 * The only scenario that the user might miss some messages is when message keeping period in
 * server expires and server removed message from database, and the application could not
 * request missing messages for some reason until then
 * (example scenarios: turned off PokoTalk application, network problem etc...)
  */
public class MessageManager {
    protected static MessageManager instance;

    public static synchronized MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }

        return instance;
    }

    /** Inspects messages */
    public void inspect() {

    }


}
