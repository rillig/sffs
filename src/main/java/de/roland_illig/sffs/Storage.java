package de.roland_illig.sffs;

import java.io.IOException;
import java.io.RandomAccessFile;

class Storage implements AutoCloseable {

    private final RandomAccessFile file;

    public Storage(RandomAccessFile file) {
        this.file = file;
    }

    public int readIntAt(long pos) throws IOException {
        file.seek(pos);
        return file.readInt();
    }

    public long readLongAt(long pos) throws IOException {
        file.seek(pos);
        return file.readLong();
    }

    public void readFully(long pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        file.seek(pos);
        file.readFully(buf, bufOffset, bufLength);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
