package de.roland_illig.sffs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The name of a directory entry.
 */
class Name {

    private final Block block;

    public Name(Block block) {
        this.block = block;
    }

    String get() throws IOException {
        var buf = new byte[block.getSize()];
        block.readFully(0, buf, 0, buf.length);
        return new String(buf, StandardCharsets.UTF_8);
    }
}
