package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Superblock {

    private final Block block;

    Superblock(Storage storage) throws IOException {
        this.block = new Block(storage, 0).checkType(BlockType.SUPER);
    }

    void setRootDirectory(Block obj) throws IOException {
        this.block.writeRef(0, obj);
    }

    Directory getRootDirectory() throws IOException {
        return new Directory(block.ref(getRootDirectoryRef()));
    }

    long getRootDirectoryRef() throws IOException {
        return block.readRef(0);
    }
}
