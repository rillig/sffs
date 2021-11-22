package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

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

    int readInt(long offset) throws IOException {
        file.seek(offset);
        return file.readInt();
    }

    long readLong(long offset) throws IOException {
        file.seek(offset);
        return file.readLong();
    }

    void readFully(long offset, byte[] buf, int bufOffset, int bufLength) throws IOException {
        file.seek(offset);
        file.readFully(buf, bufOffset, bufLength);
    }

    void write(long offset, byte[] buf, int bufOffset, int bufLength) throws IOException {
        file.seek(offset);
        file.write(buf, bufOffset, bufLength);
    }

    void writeInt(long offset, int v) throws IOException {
        file.seek(offset);
        file.writeInt(v);
    }

    void writeLong(long offset, long v) throws IOException {
        file.seek(offset);
        file.writeLong(v);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    void init() throws IOException {
        var wr = new StorageWriter(this, 0);
        Superblock.init(wr, 2, 0);
        Directory.init(wr, 2);
    }

    Name allocateName(String name) throws IOException {
        Name.check(name);

        var bytes = name.getBytes(StandardCharsets.UTF_8);
        var block = allocate(BlockType.NAME, bytes.length);
        block.write(0, bytes, 0, bytes.length);
        return new Name(block);
    }

    Directory allocateDirectory(int entries, long parentRef) throws IOException {
        var block = allocate(BlockType.DIRECTORY, 8 + entries * 16);
        block.writeLong(0, parentRef);
        return new Directory(block);
    }

    private Block allocate(BlockType type, int size) throws IOException {
        var offset = file.length();
        file.seek(offset);
        file.writeInt(type.getMagic());
        file.writeInt(size);
        file.setLength(SffsUtil.blockEnd(offset, size));
        return new Block(this, offset);
    }

    void free(long offset) throws IOException {
        assert readInt(offset) != BlockType.FREE.getMagic();
        writeInt(offset, BlockType.FREE.getMagic());
        writeLong(offset + 8, readLong(16)); // block.nextFree = super.firstFree
        writeLong(16, offset / 16); // super.firstFree = block
    }
}
