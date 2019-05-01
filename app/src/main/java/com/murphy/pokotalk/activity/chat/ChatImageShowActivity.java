package com.murphy.pokotalk.activity.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.content.ContentManager;

public class ChatImageShowActivity extends AppCompatActivity {
    private Button backspaceButton;
    private ImageView imageView;
    private TextView loadingTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_show_layout);

        // Get intent
        Intent intent = getIntent();

        // Get content name
        String contentName = intent.getStringExtra("contentName");

        // Content name show exist
        if (contentName == null) {
            finish();
            return;
        }

        // Find views
        backspaceButton = findViewById(R.id.backspaceButton);
        imageView = findViewById(R.id.image_show_image);
        loadingTextView = findViewById(R.id.image_show_loading_text);

        // Hide image view
        imageView.setVisibility(View.INVISIBLE);

        // Show image loading text
        loadingTextView.setVisibility(View.VISIBLE);
        loadingTextView.setText(R.string.image_show_loading);

        // Locate image
        ContentManager.getInstance().locateImage(this, contentName,
                new ContentManager.ImageContentLoadCallback() {
                    @Override
                    public void onLoadImage(final Bitmap image) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // Hide loading text
                                loadingTextView.setVisibility(View.GONE);

                                // Show image
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageBitmap(image);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // Show error message
                                loadingTextView.setText(R.string.image_show_fail);
                            }
                        });
                    }
                });

        // Add listeners
        backspaceButton.setOnClickListener(backspaceButtonListener);
        imageView.setOnDragListener(imageDragListener);
    }

    private View.OnClickListener backspaceButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Finish the activity
            finish();
        }
    };

    private View.OnDragListener imageDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            return false;
        }
    };
}
