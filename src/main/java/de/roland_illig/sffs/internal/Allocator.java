package de.roland_illig.sffs.internal;

class Allocator {

    private final Storage storage;

    Allocator(Storage storage) {
        this.storage = storage;
    }

    Block alloc(BlockType type, int size) {
        throw new UnsupportedOperationException();
    }

    void realloc(Block block, int size) {
        throw new UnsupportedOperationException();
    }

    void free(Block block) {
        throw new UnsupportedOperationException();
    }
}
