package com.murphy.pokotalk.data.content;

import com.murphy.pokotalk.Constants;

public class PokoImageFile extends ContentFile {
    @Override
    public String getRestPath() {
        return Constants.imagesDirectory;
    }
}
