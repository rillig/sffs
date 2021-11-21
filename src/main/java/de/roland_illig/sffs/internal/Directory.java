package de.roland_illig.sffs.internal;

import java.io.IOException;

class Directory {

    final Block block;

    Directory(Block block) {
        this.block = block;
    }

    static void init(StorageWriter wr) throws IOException {
        int entries = 16;
        wr.writeInt(BlockType.DIRECTORY.getMagic());
        wr.writeInt(8 + 16 * entries);
        wr.writeLong(0); // parent
        for (int i = 0; i < entries; i++) {
            wr.writeLong(0); // name
            wr.writeLong(0); // object
        }
    }
}
