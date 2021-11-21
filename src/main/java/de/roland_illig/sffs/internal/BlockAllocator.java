package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BlockAllocator {

    private final Allocator allocator;

    BlockAllocator(Allocator allocator) {
        this.allocator = allocator;
    }

    Name allocName(String name) throws IOException {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        Block block = allocator.alloc(BlockType.NAME, bytes.length);
        block.write(0, bytes, 0, bytes.length);
        return new Name(block);
    }
}
