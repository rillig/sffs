package de.roland_illig.sffs.internal;

final class SffsUtil {

    static int padding(int pos) {
        if (pos % 16 == 0) return 0;
        return 16 - (pos & 0x0F);
    }

    static long blockEnd(long offset, int dataSize) {
        var blockSize = 8 + dataSize;
        return U.plus(offset, blockSize + padding(blockSize));
    }
}
