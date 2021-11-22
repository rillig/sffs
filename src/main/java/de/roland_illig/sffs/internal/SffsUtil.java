package de.roland_illig.sffs.internal;

final class SffsUtil {

    static void checkRange(int off, int len, int size) {
        if (off < 0) throw new IndexOutOfBoundsException(off);
        if (len < 0) throw new IndexOutOfBoundsException(len);
        if (size < 0) throw new IndexOutOfBoundsException(size);
        if (off + len < 0) throw new IndexOutOfBoundsException(off + len);
        if (off + len > size) throw new IndexOutOfBoundsException(off + len);
    }

    static int padding(int pos) {
        if (pos % 16 == 0) return 0;
        return 16 - (pos & 0x0F);
    }

    static long blockEnd(long offset, int dataSize) {
        var blockSize = 8 + dataSize;
        return U.plus(offset, blockSize + padding(blockSize));
    }
}
