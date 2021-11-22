package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dumps the content of a single-file filesystem v1 in text format.
 */
final class Dumper {

    private final RandomAccessFile raf;
    private final StringWriter lineText = new StringWriter();
    private final PrintWriter line = new PrintWriter(lineText, true);
    private final List<String> lines = new ArrayList<>();

    public Dumper(RandomAccessFile raf) {
        this.raf = raf;
    }

    static List<String> dump(File f) throws IOException {
        try (var raf = new RandomAccessFile(f, "r")) {
            var dumper = new Dumper(raf);
            dumper.dump();
            return dumper.lines;
        }
    }

    void dump() throws IOException {
        while (raf.getFilePointer() < raf.length())
            dumpBlock();
    }

    private void dumpBlock() throws IOException {
        var blockOffset = raf.getFilePointer();
        var magic = raf.readInt();
        var type = BlockType.byMagic(magic);
        var blockSize = raf.readInt();

        println("block %d type %s size %d", blockOffset / 16, type.name(), blockSize);
        switch (type) {
            case SUPER -> dumpSuper();
            case DIRECTORY -> dumpDirectory(blockSize);
            case REGULAR -> dumpRegular();
            case FREE -> dumpFree();
            case NAME -> dumpName(blockSize);
        }

        dumpPadding(SffsUtil.blockEnd(blockOffset, blockSize));
    }

    private void dumpSuper() throws IOException {
        var rootDirRef = raf.readLong();
        var firstFreeRef = raf.readLong();
        println("    root %d firstFree %d", rootDirRef, firstFreeRef);
    }

    private void dumpDirectory(int blockSize) throws IOException {
        var parentRef = raf.readLong();
        println("    parent %d", parentRef);

        for (var pos = 8; pos < blockSize; pos += 16) {
            var nameRef = raf.readLong();
            var objRef = raf.readLong();
            if (nameRef == 0 && objRef != 0)
                println("%08x: error: nonempty directory entry %d", objRef);
            if (nameRef != 0 && objRef == 0)
                println("%08x: error: empty directory entry %d", objRef);
            if (nameRef != 0 && objRef != 0)
                println("    entry %d name %d object %d", pos / 16, nameRef, objRef);
        }
    }

    private void dumpRegular() {
        throw new UnsupportedOperationException();
    }

    private void dumpFree() {
        throw new UnsupportedOperationException();
    }

    private void dumpName(int blockSize) throws IOException {
        var bytes = new byte[blockSize];
        raf.readFully(bytes);
        println("    %s", new String(bytes, StandardCharsets.UTF_8));
    }

    private void dumpPadding(long end) throws IOException {
        for (var pos = raf.getFilePointer(); pos < end; pos++) {
            var b = raf.read();
            if (b != 0) {
                println("%08x: error: non-zero padding 0x%02x", pos, b);
            }
        }
    }

    private void println(String fmt, Object... args) {
        line.printf(Locale.ROOT, fmt, args);
        lines.add(lineText.toString());
        lineText.getBuffer().setLength(0);
    }
}
