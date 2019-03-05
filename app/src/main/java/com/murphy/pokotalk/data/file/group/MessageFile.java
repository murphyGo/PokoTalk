package com.murphy.pokotalk.data.file.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.file.PokoSequencialAccessFile;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Reader;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/** NOTE: PokoMessage file must contain message sorted by message id */
public class MessageFile extends PokoSequencialAccessFile<PokoMessage> {
    protected Group group;
    protected MessageList messageList;
    protected File messageFile;

    protected static int READ_AHEAD_LIMIT = 4096;
    protected static long READ_SIZE = 1024;
    protected long messageOffset;

    public MessageFile(Group group) {
        super();
        this.group = group;
        messageList = group.getMessageList();
        messageOffset = -1;
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
    public void openReader() throws IOException {
        Log.v("POKO", "Open reader " + getFullFilePath());
        reopenReader();
        jsonReader = new Reader() {
            @Override
            public int readChars(char[] buffer, int offset, int size) throws IOException {
                return bufferedReader.read(buffer, offset, size);
            }
        };
        bufferedReader.mark(READ_AHEAD_LIMIT);
    }

    private void reopenReader() throws IOException {
        makeSureFullDirectoryExists();
        messageFile = new File(getFullFilePath());
        fileInputStream = new FileInputStream(messageFile);
        inputStreamReader = new InputStreamReader(fileInputStream);
        bufferedReader = new BufferedReader(inputStreamReader);
    }

    /** Reads next at most n messages from back(latest order).
     * Returns the ArrayList of newly read messages. */
    public ArrayList<PokoMessage> readNextLatestMessages(int n) throws JSONException, IOException {
        int readNum, totalNum = 0;
        long skipSize, endPosition;
        ArrayList<PokoMessage> readMessages = new ArrayList<>();
        ArrayList<PokoMessage> newMessages = new ArrayList<>();

        while(totalNum < n) {
            //Log.v("POKO", "Outer most loop");
            readNum = 0;
            /* Reset file to 0 offset */
            try {
                bufferedReader.reset();
            } catch (IOException e) {
                // If reset failed, reopen file and mark at start */
                reopenReader();
                bufferedReader.mark(READ_AHEAD_LIMIT);
            }
            //Log.v("POKO", "Mark to 0 position");

            if (messageOffset >= 0) {
                skipSize = messageOffset - READ_SIZE;
                endPosition = messageOffset;
            } else {
                skipSize = messageFile.length() - READ_SIZE;
                endPosition = messageFile.length();
            }

            skipSize = skipSize < 0 ? 0 : skipSize;
            //Log.v("POKO", "Skip to " + skipSize);
            skipSize = bufferedReader.skip(skipSize);

            //Log.v("POKO", "Skip to " + skipSize);
            jsonReader.clearContext();
            if (skipSize > 0) {
                jsonReader.skipUntilNewline();
                //Log.v("POKO", "Skip new line " + (skipSize + jsonReader.getCharsRead()) +
                //        ", end pos " + endPosition);
            }

            boolean lastLoop = false;
            while(true) {
                //Log.v("POKO", "READ ONE MESSAGE");
                PokoMessage readMessage = read();
                if (readMessage == null) {
                    break;
                }

                readMessages.add(readMessage);
                long position = skipSize + jsonReader.getCharsRead();
                //Log.v("POKO", "input stream pos " + position +
                //        ", end pos " + endPosition);
                if (lastLoop) {
                    break;
                }

                // When read to endPosition, run one more loop
                // to read one more message that possibly was not read in previous loop.
                if (position >= endPosition) {
                    lastLoop = true;
                }
            }

            for (int i = readMessages.size() - 1; i >= 0; i--) {
                PokoMessage newMessage = readMessages.get(i);
                if (!messageList.updateItem(newMessage)) {
                    readNum++;
                    newMessages.add(newMessage);
                }

                if (totalNum + readNum >= n) {
                    break;
                }
            }

            totalNum += readNum;
            messageOffset = skipSize;
            if (messageOffset == 0) {
                break;
            }
        }

        return newMessages;
    }

    @Override
    public void saveItem(PokoMessage item) throws IOException, JSONException {
        JSONObject jsonMessage = Serializer.makeMessageJSON(item);
        outputStreamWriter.write(jsonMessage.toString());
    }

    @Override
    public void preSaveItem(PokoMessage item) throws IOException {
        // each json item is separated with new line character
        outputStreamWriter.write("\n");
    }
}
