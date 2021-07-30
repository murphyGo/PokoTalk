package com.murphy.pokotalk.view;

import android.content.Context;
import android.view.View;

import com.murphy.pokotalk.R;

import org.json.JSONException;
import org.json.JSONObject;

public class FileShareMessageItem extends UserMessageItem {
    public FileShareMessageItem(Context context) {
        super(context);
    }

    @Override
    public void setContent(String content) {
        // Hide other layouts
        textMessageView.setVisibility(View.GONE);
        messageImageView.setVisibility(View.GONE);

        // Show file share layout
        fileShareLayout.setVisibility(View.VISIBLE);

        try {
            if (content == null) {
                throw new JSONException("No content");
            }

            // Parse content string
            JSONObject jsonObject = new JSONObject(content);

            // Get file name
            String fileName = jsonObject.getString("fileName");

            if (fileName == null) {
                throw new JSONException("No file name");
            }

            // Show file name
            fileNameView.setText(fileName);
        } catch (JSONException e) {
            fileNameView.setText(R.string.chat_file_share_message_file_name_error);
        }
    }
}
