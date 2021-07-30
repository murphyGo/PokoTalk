package com.murphy.pokotalk.data.locationShare;

import com.murphy.pokotalk.data.list.ItemPokoList;
import com.murphy.pokotalk.data.user.User;

public class LocationShareData extends ItemPokoList<String, LocationShare> {
    @Override
    public String getKey(LocationShare locationShare) {
        User user = locationShare.getUser();
        int number = locationShare.getNumber();

        if (user != null && number >= 0) {
            return user.getUserId() + "," + number;
        } else {
            return null;
        }
    }
}
