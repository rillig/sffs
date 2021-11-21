package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Provides access to the underlying random access storage.
 * <p>
 * All "public" methods explicitly specify the position of the storage, to allow for multiple concurrent accesses
 * from a single thread, such as when copying a file.
 *
 * @see StorageWriter
 */
final class Storage implements AutoCloseable {

    private final RandomAccessFile file;

    Storage(RandomAccessFile file) throws IOException {
        this.file = file;
        if (file.length() == 0) init();
    }

    int readInt(long pos) throws IOException {
        file.seek(pos);
        return file.readInt();
    }

    long readLong(long pos) throws IOException {
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

    void init() throws IOException {
        var wr = new StorageWriter(this, 0);
        Superblock.init(wr, 1, 0);
        Directory.init(wr);
    }
}
