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

    long readRef(long offset) throws IOException {
        file.seek(offset);
        return file.readLong();
    }

    void readFully(long offset, byte[] buf, int off, int len) throws IOException {
        file.seek(offset);
        file.readFully(buf, off, len);
    }

    void write(long offset, byte[] buf, int off, int len) throws IOException {
        file.seek(offset);
        file.write(buf, off, len);
    }

    void writeInt(long offset, int v) throws IOException {
        file.seek(offset);
        file.writeInt(v);
    }

    void writeRef(long offset, long ref) throws IOException {
        file.seek(offset);
        file.writeLong(ref);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    void init() throws IOException {
        var wr = new StorageWriter(this, 0);
        var rootDir = 2L;

        wr.writeInt(BlockType.SUPER.getMagic());
        wr.writeInt(16);
        wr.writeRef(rootDir);
        wr.writeRef(0); // first free
        wr.writePadding();

        var entries = 4;
        wr.writeInt(BlockType.DIRECTORY.getMagic());
        wr.writeInt(8 + 16 * entries);
        wr.writeRef(rootDir); // parent
        for (var i = 0; i < entries; i++) {
            wr.writeRef(0); // name
            wr.writeRef(0); // object
        }
        wr.writePadding();
    }

    Block allocateName(String name) throws IOException {
        Name.check(name);

        var bytes = name.getBytes(StandardCharsets.UTF_8);
        var block = allocate(BlockType.NAME, bytes.length);
        block.write(0, bytes, 0, bytes.length);
        return block;
    }

    Block allocateDirectory(int entries, long parentRef) throws IOException {
        var block = allocate(BlockType.DIRECTORY, 8 + entries * 16);
        block.writeRef(0, parentRef);
        return block;
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
        if (SffsUtil.blockEnd(offset, readInt(offset + 4)) == file.length()) {
            file.setLength(offset);
            return;
        }
        writeInt(offset, BlockType.FREE.getMagic());
        writeRef(offset + 8, readRef(16)); // block.nextFree = super.firstFree
        writeRef(16, offset / 16); // super.firstFree = block
    }
}
