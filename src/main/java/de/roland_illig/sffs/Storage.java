package de.roland_illig.sffs;

import java.io.IOException;
import java.io.RandomAccessFile;

class Storage implements AutoCloseable {

    private final RandomAccessFile file;

    Storage(RandomAccessFile file) {
        this.file = file;
    }

    int readIntAt(long pos) throws IOException {
        file.seek(pos);
        return file.readInt();
    }

    long readLongAt(long pos) throws IOException {
        file.seek(pos);
        return file.readLong();
    }

    void readFully(long pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        file.seek(pos);
        file.readFully(buf, bufOffset, bufLength);
    }

    void write(long pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        file.seek(pos);
        file.write(buf, bufOffset, bufLength);
    }

    void writeInt(long pos, int v) throws IOException {
        file.seek(pos);
        file.writeInt(v);
    }

    void writeLong(long pos, long v) throws IOException {
        file.seek(pos);
        file.writeLong(v);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
