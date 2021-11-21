package de.roland_illig.sffs.internal;

import java.io.IOException;

class Superblock {

    static int SIZE = 16;

    private final Block block;

    Superblock(Block block) {
        this.block = block;
    }

    void setRootDirectory(Directory dir) throws IOException {
        block.writeBlockRef(0, dir.block);
    }

    Directory getRootDirectory() throws IOException {
        var offset = block.readBlockOffset(0);
        return new Directory(new Block(block, offset));
    }
}
