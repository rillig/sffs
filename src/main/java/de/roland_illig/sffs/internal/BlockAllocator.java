package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class BlockAllocator {

    private final Storage storage;

    BlockAllocator(Storage storage) {
        this.storage = storage;
    }

    Name allocName(String name) throws IOException {
        Name.check(name);

        var bytes = name.getBytes(StandardCharsets.UTF_8);
        var block = storage.createBlock(BlockType.NAME, bytes.length);
        block.write(0, bytes, 0, bytes.length);
        return new Name(block);
    }

    Directory allocDirectory(int entries, long parentRef) throws IOException {
        var block = storage.createBlock(BlockType.DIRECTORY, 8 + entries * 16);
        block.writeLong(0, parentRef);
        return new Directory(block);
    }
}
