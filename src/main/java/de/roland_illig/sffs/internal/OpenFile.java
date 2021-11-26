package de.roland_illig.sffs.internal;

import java.io.IOException;

/**
 * A file that is currently opened.
 * <p>
 * On Windows, open files cannot be removed.
 * <p>
 * On POSIX, open files can be removed, they just lose their name, but their data can still be read or written. When
 * the file is not open anymore, the file data gets deallocated as well.
 */
final class OpenFile implements de.roland_illig.sffs.OpenFile {

    private final RegularFile regularFile;
    private final boolean canRead;
    private final boolean canWrite;

    private long offset;

    OpenFile(RegularFile regularFile, String mode) {
        this.regularFile = regularFile;
        this.canRead = mode.equals("r");
        this.canWrite = mode.equals("w");
        // TODO: register the open file in the filesystem
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (!canRead)
            throw new IOException("write-only");
        var n = regularFile.read(offset, buf, off, len);
        offset += n;
        return n;
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        if (!canWrite)
            throw new IOException("read-only");
        regularFile.write(offset, buf, off, len);
        offset += len;
    }

    @Override
    public void seek(long offset) {
        this.offset = offset;
    }

    @Override
    public long tell() {
        return offset;
    }

    @Override
    public void close() throws IOException {
        // TODO: unregister the open file from the filesystem
    }
}
