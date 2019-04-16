package com.murphy.pokotalk.activity.settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.murphy.pokotalk.R;

public class ImageEditionActivity extends AppCompatActivity {
    private Button backspaceButton;
    private ImageView imageView;
    private Button rotateButton;
    private Button imageSelectButton;
    public static Bitmap image;

    public static final int MAX_WIDTH = 640;
    public static final int MAX_HEIGHT = 640;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edition_layout);

        // Find views
        backspaceButton = findViewById(R.id.backspaceButton);
        imageView = findViewById(R.id.image_edition_image);
        rotateButton = findViewById(R.id.image_edition_rotate_button);
        imageSelectButton = findViewById(R.id.image_edition_select_button);

        // Image should exist
        if (image == null) {
            Toast.makeText(this, "No image!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Show image
        imageView.setImageBitmap(image);

        // Add event listeners
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rotateButton.setOnClickListener(rotateButtonListener);
        imageSelectButton.setOnClickListener(selectButtonListener);
    }

    private View.OnClickListener rotateButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.image_edit_button_animation2);

            // Load button animation
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.image_edit_button_animation);

            animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rotateButton.startAnimation(animation2);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

            // Set duration
            animation.setDuration(175);
            animation2.setDuration(175);

            // Show animation
            rotateButton.startAnimation(animation);

            // Create matrix for rotation
            Matrix matrix = new Matrix();

            // rotation degree is 90
            matrix.postRotate(90.0f);

            // Produce rotated image
            image = Bitmap.createBitmap(image, 0, 0,
                    image.getWidth(), image.getHeight(), matrix, true);

            // Show new image
            imageView.setImageBitmap(image);
        }
    };

    private View.OnClickListener selectButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get width and heights of image
            float width = (float) image.getWidth();
            float height = (float) image.getHeight();

            // Check if image is too big so we need to resize it
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                // Find scale factor
                float scaleFactor = Math.min(MAX_WIDTH / width, MAX_HEIGHT / height);

                // Scale image
                image = Bitmap.createScaledBitmap(image,
                        (int) (width * scaleFactor), (int) (height * scaleFactor), false);
            }

            // Put result image and finish the activity
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
