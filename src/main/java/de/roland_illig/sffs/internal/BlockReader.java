package de.roland_illig.sffs.internal;

import java.io.IOException;

final class BlockReader {

    private final Block block;
    private int pos;

    BlockReader(Block block, int pos) {
        this.block = block;
        this.pos = pos;
    }

    int getPos() {
        return pos;
    }

    boolean hasNext() throws IOException {
        return pos < block.getSize();
    }

    long readRef() throws IOException {
        var ref = block.readRef(pos);
        pos += 8;
        return ref;
    }
}
