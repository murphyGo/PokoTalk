package com.murphy.pokotalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity {
    private CircleImageView pokoImage;
    private TextView appNameText;
    private final int splash_time = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pokoImage = (CircleImageView) findViewById(R.id.pokoImage);
        appNameText = (TextView) findViewById(R.id.appNameText);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.welcome_action);
        pokoImage.startAnimation(anim);
        appNameText.startAnimation(anim);

        final Intent intent = new Intent(this, MainActivity.class);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try{
                    sleep(splash_time);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                finish();
            }
        };

        thread.start();
    }
}
