package de.roland_illig.sffs.internal;

enum BlockType {
    SUPER("SFsu"),
    DIRECTORY("SFdi"),
    DIRECTORY_ENTRY("SFde"),
    REGULAR("SFre"),
    FREE("SFfr"),
    NAME("SFnm");

    private final int magic;

    BlockType(String magic) {
        this.magic = magic(magic);
    }

    private int magic(String magic) {
        assert magic.length() == 4;
        return magic.charAt(0) << 24 | magic.charAt(1) << 16 | magic.charAt(2) << 8 | magic.charAt(3);
    }

    int getMagic() {
        return magic;
    }
}
