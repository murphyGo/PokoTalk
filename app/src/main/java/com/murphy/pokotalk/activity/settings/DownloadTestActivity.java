package com.murphy.pokotalk.activity.settings;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.server.content.ContentManager;

public class DownloadTestActivity extends AppCompatActivity {
    private Button downloadButton;
    private EditText contentNameView;
    private ImageView imageView1;
    private ImageView imageView2;
    private boolean imageView1Loaded = false;

    private static final String defaultContent = "dfd8a2fa05bb2ca6.jpg";
    private static final String defaultContent2 = "eadc1bbdf2ac26a1.jpeg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_layout);

        downloadButton = findViewById(R.id.contentLoadButton);
        contentNameView = findViewById(R.id.contentName);
        imageView1 = findViewById(R.id.contentImage1);
        imageView2 = findViewById(R.id.contentImage2);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentName = contentNameView.getText().toString().trim();

                if (contentName.length() == 0) {
                    if (imageView1Loaded) {
                        contentName = defaultContent2;
                    } else {
                        contentName = defaultContent;
                    }
                }

                ContentManager contentManager = ContentManager.getInstance();

                contentManager.locateImage(getApplicationContext(),
                        contentName,
                        new ContentManager.ImageContentLoadCallback() {
                            @Override
                            public void onLoadImage(final Bitmap image) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (imageView1Loaded) {
                                            imageView2.setImageBitmap(image);
                                        } else {
                                            imageView1.setImageBitmap(image);
                                            imageView1Loaded = true;
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Download failed, try again later",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });
    }
}
