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

    int getChunkSize() throws IOException {
        return block.readInt(8);
    }

    void setChunkSize(int chunkSize) throws IOException {
        block.writeInt(8, chunkSize);
    }

    int read(long offset, byte[] buf, int off, int len) throws IOException {
        SffsUtil.checkRange(off, len, Integer.MAX_VALUE);
        if (offset < 0)
            throw new IndexOutOfBoundsException(offset);
        if (24 + offset + len > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException(offset + len);

        if (getChunkSize() == 0)
            return block.read(24 + (int) offset, buf, off, len);
        else
            return readLarge(offset, buf, off, len);
    }

    private int readLarge(long offset, byte[] buf, int off, int len) {
        throw new UnsupportedOperationException();
    }

    void write(long offset, byte[] buf, int off, int len) throws IOException {
        SffsUtil.checkRange(off, len, Integer.MAX_VALUE);
        var end = 24 + offset + len;
        if (offset < 0)
            throw new IndexOutOfBoundsException(offset);
        if (end > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException(offset + len);

        if (getChunkSize() == 0 && end > block.getSize())
            enlarge(offset + len);

        if (getChunkSize() == 0)
            block.write(24 + (int) offset, buf, off, len);
        else
            writeLarge(offset, buf, off, len);

        if (offset + len > getSize())
            setSize(offset + len);
    }

    private void writeLarge(long offset, byte[] buf, int off, int len) {
        throw new UnsupportedOperationException();
    }

    private void enlarge(long fileSize) {
        throw new UnsupportedOperationException();
    }
}
