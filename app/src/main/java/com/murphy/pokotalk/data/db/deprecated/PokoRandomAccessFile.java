package com.murphy.pokotalk.data.db.deprecated;

import android.util.Log;

import com.murphy.pokotalk.data.db.json.Reader;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/** File using RandomAccessFile that can be randomly accessed and can read and write at the same time.
 * It does not have flush because it directly write to device.
 */
public abstract class PokoRandomAccessFile<T> extends PokoMultiItemsFile<T> {
    protected RandomAccessFile randomAccessFile;

    public void openFile() throws IOException {
        Log.v("POKO", "Open reader " + getFullFilePath());
        makeSureFullDirectoryExists();
        randomAccessFile = new RandomAccessFile(new File(getFullFilePath()), "rw");
        jsonReader = new Reader() {
            @Override
            public int readChars(char[] buffer, int offset, int size) throws IOException {
                int len = 0;
                try {
                    while (len < size) {
                        char c = randomAccessFile.readChar();
                        buffer[offset++] = c;
                        len++;
                    }
                } catch (EOFException e) {

                }

                return len;
            }
        };
    }

    public void closeFile() throws IOException {
        Log.v("POKO", "close reader");
        randomAccessFile.close();
    }
}
