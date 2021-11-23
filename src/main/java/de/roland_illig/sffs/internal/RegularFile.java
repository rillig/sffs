package de.roland_illig.sffs.internal;

import java.io.IOException;

/**
 * A sequence of bytes on the disk.
 */
final class RegularFile {

    private final Block block;

    RegularFile(Block block) {
        this.block = block;
    }

    long getSize() throws IOException {
        return block.readLong(0);
    }

    void setSize(long size) throws IOException {
        block.writeLong(0, size);
    }

    int read(long offset, byte[] buf, int off, int len) throws IOException {
        assert offset < block.getSize();
        throw new UnsupportedOperationException();
    }

    void write(long offset, byte[] buf, int off, int len) throws IOException {
        SffsUtil.checkRange(off, len, block.getSize());
        if (offset < 0)
            throw new IndexOutOfBoundsException(offset);
        if (8 + offset + len > block.getSize())
            throw new IndexOutOfBoundsException(offset + len);

        block.write(8 + (int) offset, buf, off, len);
        if (offset + len > getSize())
            setSize(offset + len);
    }
}
