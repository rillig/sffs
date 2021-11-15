package de.roland_illig.sffs;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Storage {

    private final RandomAccessFile file;

    public Storage(RandomAccessFile file) {
        this.file = file;
    }

    public int readIntAt(long pos) {
        try {
            file.seek(pos);
            return file.readInt();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public long readLongAt(long pos) {
        try {
            file.seek(pos);
            return file.readLong();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public void readFully(long pos, byte[] buf, int bufOffset, int bufLength) {
        try {
            file.seek(pos);
            file.readFully(buf, bufOffset, bufLength);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}
