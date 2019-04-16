package com.murphy.pokotalk.data.content;

import com.murphy.pokotalk.Constants;

public class PokoBinaryFile extends ContentFile {
    @Override
    public String getRestPath() {
        return Constants.binaryContentDirectory;
    }
}
