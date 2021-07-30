package com.murphy.pokotalk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.murphy.pokotalk.R;

public class LocationShareUserSelectItem extends FrameLayout {
    private View view;
    private String name;
    private Bitmap img;
    private TextView nameView;
    private ImageView imageView;
    private Context context;

    public LocationShareUserSelectItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.location_share_select_user_item, this, true);

        nameView = view.findViewById(R.id.locationShareSelectUserName);
        imageView = view.findViewById(R.id.locationShareSelectUserImage);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        nameView.setText(name);
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
        imageView.setImageBitmap(img);
    }
}
