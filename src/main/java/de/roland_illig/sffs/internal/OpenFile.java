package de.roland_illig.sffs.internal;

/**
 * A file that is currently opened.
 * <p>
 * On Windows, open files cannot be removed.
 * <p>
 * On POSIX, open files can be removed, they just lose their name, but their data can still be read or written. When
 * the file is not open anymore, the file data gets deallocated as well.
 */
final class OpenFile {

    private final RegularFile regularFile;

    private long offset;

    OpenFile(RegularFile regularFile) {
        this.regularFile = regularFile;
    }

    int read(byte[] buf, int off, int len) {
        throw new UnsupportedOperationException();
    }

    void write(byte[] buf, int off, int len) {
        throw new UnsupportedOperationException();
    }

    void seek(long offset) {
        this.offset = offset;
    }

    long tell() {
        return offset;
    }
}
