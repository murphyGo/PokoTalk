package com.murphy.pokotalk.content;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ContentStream {
    private Context context;
    private InputStream contentStream;

    private static final int CHUNK_SIZE = 4096;

    public ContentStream(Context context, ContentResolver resolver, Uri uri) throws FileNotFoundException {
        this.context = context;

        // Get input stream for content
        contentStream = resolver.openInputStream(uri);
    }

    public byte[] getNextChunk() throws IOException {
        // Get size of chunk
        int size = CHUNK_SIZE;

        // Allocate buffer
        byte[] buffer = new byte[size];

        // Initialize offset
        int offset = 0;

        // Read size of bytes
        while (size > 0) {
            // Read bytes
            int n = contentStream.read(buffer, offset, size);

            // Check EOF
            if (n <= 0) {
                // Check if no data read
                if (offset == 0) {
                    return null;
                } else {
                    break;
                }
            }

            // Adjust offset and read length
            size -= n;
            offset += n;
        }

        // Clip array if not all bytes are read
        if (size > 0) {
            buffer = Arrays.copyOfRange(buffer, 0, offset);
        }

        return buffer;
    }

    public void close() {
        try {
            if (contentStream != null) {
                contentStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
