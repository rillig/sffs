package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Allocator {

    private final Storage storage;

    Allocator(Storage storage) {
        this.storage = storage;
    }

    Block alloc(BlockType type, int size) throws IOException {
        throw new UnsupportedOperationException();
    }

    void realloc(Block block, int size) throws IOException {
        throw new UnsupportedOperationException();
    }

    void free(Block block) throws IOException {
        throw new UnsupportedOperationException();
    }
}
