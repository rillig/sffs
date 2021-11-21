package de.roland_illig.sffs.internal;

import java.io.IOException;

enum BlockType {
    SUPER("SF01"),
    DIRECTORY("SFdi"),
    DIRECTORY_ENTRY("SFde"),
    REGULAR("SFre"),
    FREE("SFfr"),
    NAME("SFnm");

    private final int magic;

    BlockType(String magic) {
        this.magic = magic(magic);
    }

    static BlockType byMagic(int magic) throws IOException {
        for (var type : values())
            if (type.magic == magic)
                return type;
        throw new IOException(String.format("Invalid magic number 0x%08x", magic));
    }

    private int magic(String magic) {
        assert magic.length() == 4;
        return magic.charAt(0) << 24 | magic.charAt(1) << 16 | magic.charAt(2) << 8 | magic.charAt(3);
    }

    int getMagic() {
        return magic;
    }
}
