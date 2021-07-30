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
    private int size;
    private Uri uri;

    private static final int CHUNK_SIZE = 1024 * 1024;

    public ContentStream(Context context, ContentResolver resolver, Uri uri) throws FileNotFoundException {
        this.context = context;
        this.uri = uri;

        // Get input stream for content
        contentStream = resolver.openInputStream(uri);

        // Get file size
        size = ContentReader.getFileSize(resolver, uri);

        // File size should exist
        if (size < 0) {
            throw new FileNotFoundException("Can not find size of file");
        }
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
                contentStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getSize() {
        return size;
    }

    public Uri getUri() {
        return uri;
    }
}
