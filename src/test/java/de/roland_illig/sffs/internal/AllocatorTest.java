package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AllocatorTest {

    @Test
    void alloc(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");
        var raf = new RandomAccessFile(f, "rw");
        var storage = new Storage(raf);
        var allocator = new Allocator(storage);

        var block = allocator.alloc(BlockType.SUPER, 8);
        var name = allocator.alloc(BlockType.NAME, 15);

        storage.close();
    }
}
