package com.murphy.pokotalk.data.file.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

/** Reads json data from file
 * the file should be concatenation of json object strings */
public abstract class Reader {
    protected TokenContext tokenContext;
    protected StringBuffer stringBuffer;
    protected ArrayDeque<JSONObject> jsonObjects;
    protected long charsRead;

    public static final int CHAR_BUFFER_SIZE = 128;
    protected char[] charBuffer;

    public Reader() {
        tokenContext = new TokenContext();
        stringBuffer = new StringBuffer();
        jsonObjects = new ArrayDeque<>();
        charBuffer = new char[CHAR_BUFFER_SIZE];
        clearContext();
    }

    public void clearContext() {
        tokenContext.clear();
        stringBuffer.setLength(0);
        jsonObjects.clear();
        charsRead = 0;
    }


    /* Read n JSONObjects from reader */
    public ArrayList<JSONObject> readJSONs(int n) throws IOException, JSONException {
        ArrayList<JSONObject> result = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            result.add(readJSON());
        }

        return result;
    }

    /* Should override this methods. This method reads data from file input
     * and write to buffer, start from offset, maximum size of size.
     */
    public abstract int readChars(char[] buffer, int offset, int size) throws IOException;

    protected int isUTF8StartingByte(byte b) {
        if ((b & (byte) 0x80) == 0) {
            return 0;
        } else if ((b & (byte) 0xe0) == 0xc0) {
            return 1;
        } else if ((b & (byte) 0xf0) == 0xe0) {
            return 2;
        } else if ((b & (byte) 0xf8) == 0xf0) {
            return 3;
        }
        return -1;
    }

    public void clearCharsRead() {
        charsRead = 0;
    }

    public long getCharsRead() {
        return charsRead;
    }

    /* Reads and discards characters until it meets newline character */
    public boolean skipUntilNewline() throws IOException, JSONException {
        int n;

        while ((n = readChars(charBuffer, 0, charBuffer.length)) > 0) {
            int offset;
            char c;
            charsRead += n;
            for (offset = 0; offset < n; offset++) {
                c = charBuffer[offset];
                if (c == '\n') {
                    putBufferToContext(n,offset + 1);
                    return true;
                }
            }
        }

        return false;
    }

    /** Put bytes of buffer of size 'n' to TokenContext starting from offset 'len'
     * and add all generated JSONObject to list.
     * */
    protected void putBufferToContext(int n, int len) throws JSONException {
        int offset;
        while ((offset = tokenContext.putCharacterArray(charBuffer, len, n - len)) >= 0) {
            stringBuffer.append(charBuffer, len, offset + 1);
            len += offset + 1;
            JSONObject result = new JSONObject(stringBuffer.toString());
            stringBuffer.setLength(0);
            jsonObjects.addLast(result);
        }
        stringBuffer.append(charBuffer, len, n - len);
    }

    /* Reads one json object from input */
    public JSONObject readJSON() throws IOException, JSONException {
        if (jsonObjects.size() > 0) {
            return jsonObjects.removeFirst();
        }

        int n;
        while ((n = readChars(charBuffer, 0, charBuffer.length)) > 0) {
            //Log.v("POKO", "READ " + n + " bytes");
            charsRead += n;
            putBufferToContext(n, 0);
            if (jsonObjects.size() > 0) {
                return jsonObjects.removeFirst();
            }
        }

        if (stringBuffer.length() > 0 && !tokenContext.isValidJSON()) {
            //Log.v("POKO", stringBuffer.toString());
            throw new JSONException("Bad json data");
        }

        return null;
    }

    class TokenContext {
        public boolean meetStart;
        public boolean isEscape;
        public boolean isStringContext;
        protected ArrayDeque<Character> tokenDeque;

        public TokenContext() {
            clear();
        }

        public void clear() {
            meetStart = false;
            isEscape = false;
            isStringContext = false;
            if (tokenDeque == null) {
                tokenDeque = new ArrayDeque<>();
            } else {
                tokenDeque.clear();
            }
        }

        /* Put character list and returns first index from offset that made valid json string.
         * If it can not find right index, returns -1. */
        public int putCharacterArray(char[] chars, int offset, int size) throws JSONException {
            for (int i = offset; i < offset + size; i++) {
                if (putToken(chars[i])) {
                    return i - offset;
                }
            }
            return -1;
        }

        /* Put a token and returns true if it became a valid json, false otherwise */
        public boolean putToken(char token) throws JSONException {
            if (!meetStart) {
                if (Character.isWhitespace(token))
                    return false;

                switch(token) {
                    case '{':
                    case '[':
                        meetStart = true;
                        break;
                    default:
                        throw new JSONException("Unexpected token");
                }
            }

            if (isEscape) {
                isEscape = false;
                return false;
            }

            if (isStringContext) {
                switch (token) {
                    case '"':
                    case '\'':
                        if (tokenDeque.getLast() == token) {
                            tokenDeque.removeLast();
                            isStringContext = false;
                        }
                        break;
                    case '\\':
                        isEscape = true;
                        break;
                    default:
                        break;
                }
            } else {
                switch(token) {
                    case '{':
                    case'[':
                        tokenDeque.addLast(token);
                        break;
                    case '}':
                    case ']':
                        if (tokenDeque.getLast() == getMirroredToken(token)) {
                            tokenDeque.removeLast();
                            break;
                        } else {
                            throw new JSONException("Bad closing token");
                        }
                    case ':':
                        break;
                    case '"':
                    case'\'':
                        isStringContext = true;
                        tokenDeque.addLast(token);
                        break;
                    default:
                        break;
                }
            }

            return isValidJSON();
        }

        public boolean isValidJSON() {
            /* Token should be empty */
            if (tokenDeque.size() == 0) {
                clear();
                return true;
            }

            return false;
        }

        public char getMirroredToken(char token) throws JSONException {
            switch(token) {
                case '}':
                    return '{';
                case ']':
                    return '[';
                case '{':
                    return '}';
                case '[':
                    return ']';
            }

            throw new JSONException("Non mirrored token");
        }

    }
}
