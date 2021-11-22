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
    private final BlockAllocator blockAllocator;

    Storage(RandomAccessFile file) throws IOException {
        this.file = file;
        if (file.length() == 0) init();
        this.blockAllocator = new BlockAllocator(new Allocator(this));
    }

    // TODO: rename 'pos' to 'offset'
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
        Superblock.init(wr, 2, 0);
        Directory.init(wr, 2);
    }

    Name allocName(String name) throws IOException {
        return blockAllocator.allocName(name);
    }

    Directory allocDirectory(int entries, long parentRef) throws IOException {
        return blockAllocator.allocDirectory(entries, parentRef);
    }

    Block createBlock(BlockType type, int size) throws IOException {
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
