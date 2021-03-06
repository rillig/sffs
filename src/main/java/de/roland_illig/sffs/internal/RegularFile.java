package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.util.Arrays;

/**
 * A sequence of bytes on the disk.
 */
final class RegularFile {

    private final Block block;

    RegularFile(Block block) throws IOException {
        this.block = block.checkType(BlockType.REGULAR);
    }

    long getSize() throws IOException {
        return block.readLong(0);
    }

    void setSize(long size) throws IOException {
        block.writeLong(0, size);
    }

    private int getChunkSize() throws IOException {
        return block.readInt(8);
    }

    private void setChunkSize(int chunkSize) throws IOException {
        block.writeInt(8, chunkSize);
    }

    private Block getChunkForReading(int chunkIndex) throws IOException {
        var ref = block.readRef(chunkPos(chunkIndex));
        return ref != 0 ? block.block(ref, BlockType.CHUNK) : null;
    }

    private Block getChunkForWriting(int chunkIndex) throws IOException {
        var ref = block.readRef(chunkPos(chunkIndex));
        if (ref != 0) return block.block(ref, BlockType.CHUNK);
        var block = this.block.storage.allocateChunk(getChunkSize());
        setChunk(chunkIndex, block);
        return block;
    }

    private int chunkPos(int chunkIndex) {
        return Math.addExact(24, Math.multiplyExact(chunkIndex, 8));
    }

    private void setChunk(int chunkIndex, Block chunk) throws IOException {
        block.writeRef(chunkPos(chunkIndex), chunk);
    }

    int read(long offset, byte[] buf, int off, int len) throws IOException {
        SffsUtil.checkRange(off, len, Integer.MAX_VALUE);
        if (offset < 0)
            throw new IndexOutOfBoundsException(offset);
        if (24 + offset + len > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException(offset + len);

        if (len == 0)
            return 0;
        var maxLen = Math.toIntExact(Math.min(len, getSize() - offset));
        if (maxLen <= 0)
            return -1;

        if (getChunkSize() == 0)
            return block.read(24 + (int) offset, buf, off, maxLen);
        else
            return readLarge(offset, buf, off, maxLen);
    }

    private int readLarge(long offset, byte[] buf, int off, int len) throws IOException {
        var end = offset + len;
        var chunkSize = getChunkSize();
        var chunkStartIndex = Math.toIntExact(offset / chunkSize);
        var chunkEndIndex = Math.toIntExact(end / chunkSize);

        if (chunkStartIndex == chunkEndIndex)
            return readFromChunk(chunkStartIndex, (int) (offset % chunkSize), buf, off, len);

        var partOff = (int) (offset % chunkSize);
        var partLen = chunkSize - partOff;

        var totalRead = readFromChunk(chunkStartIndex, partOff, buf, off, partLen);
        for (var i = chunkStartIndex + 1; i < chunkEndIndex; i++)
            totalRead += readFromChunk(i, 0, buf, off + totalRead, chunkSize);
        if (totalRead < len)
            totalRead += readFromChunk(chunkEndIndex, 0, buf, off + totalRead, len - totalRead);

        return totalRead;
    }

    private int readFromChunk(int chunkIndex, int chunkOffset, byte[] buf, int off, int len) throws IOException {
        var chunk = getChunkForReading(chunkIndex);
        if (chunk != null) return chunk.read(8 + chunkOffset, buf, off, len);
        Arrays.fill(buf, off, off + len, (byte) 0);
        return len;
    }

    void write(long offset, byte[] buf, int off, int len) throws IOException {
        SffsUtil.checkRange(off, len, Integer.MAX_VALUE);
        var end = 24 + offset + len;
        if (offset < 0)
            throw new IndexOutOfBoundsException(offset);
        if (end > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException(offset + len);

        if (getChunkSize() == 0 && end > block.getSize())
            enlarge();

        if (getChunkSize() == 0)
            block.write(24 + (int) offset, buf, off, len);
        else
            writeLarge(offset, buf, off, len);

        if (offset + len > getSize())
            setSize(offset + len);
    }

    void delete() throws IOException {
        if (getChunkSize() != 0) {
            for (int pos = 24, size = block.getSize(); pos < size; pos += 8) {
                var ref = block.readRef(pos);
                if (ref != 0) block.block(ref).free();
            }
        }
        block.free();
    }

    private void writeLarge(long offset, byte[] buf, int off, int len) throws IOException {
        var end = offset + len;
        var chunkSize = getChunkSize();
        var chunkStartIndex = Math.toIntExact(offset / chunkSize);
        var chunkEndIndex = Math.toIntExact(end / chunkSize);

        if (chunkStartIndex == chunkEndIndex) {
            writeToChunk(chunkStartIndex, (int) (offset % chunkSize), buf, off, len);
            return;
        }

        var partOff = (int) (offset % chunkSize);
        var partLen = chunkSize - partOff;

        writeToChunk(chunkStartIndex, partOff, buf, off, partLen);
        var totalWritten = partLen;

        for (var i = chunkStartIndex + 1; i < chunkEndIndex; i++) {
            writeToChunk(i, 0, buf, off + totalWritten, chunkSize);
            totalWritten += chunkSize;
        }

        if (totalWritten < len)
            writeToChunk(chunkEndIndex, 0, buf, off + totalWritten, len - totalWritten);
    }

    private void writeToChunk(int chunkIndex, int chunkOffset, byte[] buf, int off, int len) throws IOException {
        var chunk = getChunkForWriting(chunkIndex);
        chunk.write(8 + chunkOffset, buf, off, len);
    }

    private void enlarge() throws IOException {
        var data = new byte[Math.toIntExact(getSize())];
        block.readFully(24, data, 0, data.length);
        block.write(24, new byte[data.length], 0, data.length);

        setChunkSize(4096);

        writeLarge(0, data, 0, data.length);
    }
}
