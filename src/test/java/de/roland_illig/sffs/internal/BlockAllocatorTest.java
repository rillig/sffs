package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlockAllocatorTest {

    @Test
    void allocName(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");
        var raf = new RandomAccessFile(f, "rw");
        var storage = new Storage(raf);
        var blockAllocator = new BlockAllocator(storage);

        var block = blockAllocator.allocName("directory name");

        storage.close();
    }
}
