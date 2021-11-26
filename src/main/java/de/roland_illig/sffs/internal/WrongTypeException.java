package de.roland_illig.sffs.internal;

import java.nio.file.FileSystemException;
import java.nio.file.Path;

class WrongTypeException extends FileSystemException {

    final BlockType expected;
    final BlockType got;

    WrongTypeException(Path path, BlockType expected, BlockType got) {
        super(path.toString(), null, "expected " + expected + ", got " + got);
        this.expected = expected;
        this.got = got;
    }

    WrongTypeException(BlockType expected, BlockType got) {
        super(null, null, "expected " + expected + ", got " + got);
        this.expected = expected;
        this.got = got;
    }

    WrongTypeException(Path path, WrongTypeException cause) {
        this(path, cause.expected, cause.got);
        initCause(cause);
    }
}
