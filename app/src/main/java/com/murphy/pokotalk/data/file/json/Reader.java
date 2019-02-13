package com.murphy.pokotalk.data.file.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

/** Reads json data from file
 * the file should be concatenation of json object strings */
public class Reader {
    protected TokenContext tokenContext;
    protected BufferedReader bufferedReader;
    protected StringBuffer stringBuffer;
    protected ArrayDeque<JSONObject> jsonObjects;

    public static final int BUFFER_SIZE = 32;

    public Reader(BufferedReader reader) {
        bufferedReader = reader;
        tokenContext = new TokenContext();
        stringBuffer = new StringBuffer();
        jsonObjects = new ArrayDeque<>();
    }

    public void clearContext() {
        tokenContext.clear();
        stringBuffer.setLength(0);
        jsonObjects.clear();
    }

    /* Read n JSONObjects from reader */
    public ArrayList<JSONObject> readJSONs(int n) throws IOException, JSONException {
        ArrayList<JSONObject> result = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            result.add(readJSON());
        }

        return result;
    }

    /* Reads one json object from input */
    public JSONObject readJSON() throws IOException, JSONException {
        if (jsonObjects.size() > 0) {
            return jsonObjects.removeFirst();
        }

        char[] buffer = new char[BUFFER_SIZE];

        int n, offset, len = 0;
        while ((n = bufferedReader.read(buffer, 0, buffer.length)) > 0) {
            while ((offset = tokenContext.putCharacterArray(buffer, len, n - len)) >= 0) {
                stringBuffer.append(buffer, len, len + offset + 1);
                len += offset + 1;
                JSONObject result = new JSONObject(stringBuffer.toString());
                stringBuffer.setLength(0);
                jsonObjects.addLast(result);
            }
            stringBuffer.append(buffer, len, n - len);
            if (jsonObjects.size() > 0) {
                return jsonObjects.removeFirst();
            }
            len = 0;
        }

        if (stringBuffer.length() > 0 && !tokenContext.isValidJSON()) {
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
