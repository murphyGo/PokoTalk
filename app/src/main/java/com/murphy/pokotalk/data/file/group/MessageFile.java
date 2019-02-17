package com.murphy.pokotalk.data.file.group;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.file.PokoMultiItemsFile;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.group.MessageList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/** NOTE: PokoMessage file must contain message sorted by message id */
public class MessageFile extends PokoMultiItemsFile<PokoMessage> {
    protected Group group;
    protected MessageList messageList;
    protected int position;

    public MessageFile(Group group) {
        super();
        this.group = group;
        messageList = group.getMessageList();
        position = 0;
    }

    @Override
    public String getFileName() {
        return String.format(Constants.locale, Constants.messageFileFormat, group.getGroupId());
    }

    @Override
    public String getRestPath() {
        return super.getRestPath() + File.separator + Constants.messageDirectory;
    }

    @Override
    public int getItemListSize() {
        return messageList.getList().size();
    }

    @Override
    public void addItem(PokoMessage item) {
        messageList.updateItem(item);
    }

    @Override
    public PokoMessage getItemAt(int position) {
        return messageList.getList().get(position);
    }

    @Override
    public PokoMessage read() throws IOException, JSONException {
        JSONObject jsonMessage = readJSON();
        if (jsonMessage == null) {
            return null;
        }

        return Parser.parseMessage(jsonMessage);
    }

    @Override
    public void saveItem(PokoMessage item) throws IOException, JSONException {
        JSONObject jsonMessage = Serializer.makeMessageJSON(item);
        outputStreamWriter.write(jsonMessage.toString());
    }
}
