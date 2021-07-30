package com.murphy.pokotalk.view;

import android.content.Context;
import android.view.View;

public class TextMessageItem extends UserMessageItem {
    public TextMessageItem(Context context) {
        super(context);
    }

    public void setContent(String content) {
        this.content = content;

        // Hide image view and show message view
        messageImageView.setVisibility(View.GONE);
        fileShareLayout.setVisibility(View.GONE);
        textMessageView.setVisibility(View.VISIBLE);

        // Set content
        textMessageView.setText(content);
    }
}

