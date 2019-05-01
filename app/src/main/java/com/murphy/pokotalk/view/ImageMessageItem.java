package com.murphy.pokotalk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.content.ContentManager;

public class ImageMessageItem extends UserMessageItem {
    public ImageMessageItem(Context context) {
        super(context);
    }

    @Override
    public void setContent(String content) {
        // Hide message view and show image view
        textMessageView.setVisibility(View.GONE);
        messageImageVihew.setVisibility(View.VISIBLE);

        if (content != null) {
            // Locate image content
            ContentManager.getInstance().locateThumbnailImage(context,
                    content,
                    new ContentManager.ImageContentLoadCallback() {
                        @Override
                        public void onError() {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Hide image view and show text view
                                            textMessageView.setVisibility(View.VISIBLE);
                                            messageImageVihew.setVisibility(View.GONE);

                                            // Set error message
                                            textMessageView.setText(R.string.chat_locate_image_failed);
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onLoadImage(final Bitmap image) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(image != null) {
                                        // Show image
                                        messageImageVihew.setImageBitmap(image);
                                    }
                                }
                            });
                        }
                    });
        }
    }
}
