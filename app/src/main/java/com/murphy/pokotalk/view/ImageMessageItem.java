package com.murphy.pokotalk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.content.ContentManager;

public class ImageMessageItem extends UserMessageItem {
    protected ContentManager.ImageContentLoadCallback messageImageLocateCallback;

    public ImageMessageItem(Context context) {
        super(context);
    }

    @Override
    public void setContent(String content) {
        // Hide message view and show image view
        textMessageView.setVisibility(View.GONE);
        fileShareLayout.setVisibility(View.GONE);
        messageImageView.setVisibility(View.VISIBLE);

        // Cancel and remove image content locate callbacks
        if (messageImageLocateCallback != null) {
            messageImageLocateCallback.cancel();
            messageImageLocateCallback = null;
        }

        messageImageLocateCallback = new ContentManager.ImageContentLoadCallback() {
            @Override
            public void onError() {
                // Hide image view and show text view
                textMessageView.setVisibility(View.VISIBLE);
                messageImageView.setVisibility(View.GONE);

                // Set error message
                textMessageView.setText(R.string.chat_locate_image_failed);
            }

            @Override
            public void onLoadImage(final Bitmap image) {
                if(image != null) {
                    // Show image
                    messageImageView.setImageBitmap(image);
                }
            }
        };

        if (content != null) {
            // Locate image content
            ContentManager.getInstance().locateThumbnailImage(context,
                    content,
                    messageImageLocateCallback);
        }
    }
}
