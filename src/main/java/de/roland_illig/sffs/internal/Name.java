package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The name of a directory entry.
 */
final class Name {

    private final Block block;

    Name(Block block) {
        this.block = block;
    }

    String get() throws IOException {
        var buf = new byte[block.getSize()];
        block.readFully(0, buf, 0, buf.length);
        return new String(buf, StandardCharsets.UTF_8);
    }

    static void check(String name) throws IOException {
        checkForbidden(name, '\0');
        checkForbidden(name, '/');
        checkForbidden(name, '\\');
        checkForbidden(name, "");
        checkForbidden(name, ".");
        checkForbidden(name, "..");
    }

    private static void checkForbidden(String name, int ch) throws IOException {
        if (name.indexOf(ch) != -1) throw new IOException(String.format("Invalid character U+%04X", ch));
    }

    private static void checkForbidden(String name, String forbidden) throws IOException {
        if (name.equals(forbidden)) throw new IOException(String.format("Invalid name \"%s\"", name));
    }
}
