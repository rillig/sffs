package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AllocatorTest {

    private Storage storage;
    private Allocator testee;

    @BeforeEach
    void setUp(@TempDir File tmpdir) throws FileNotFoundException {
        var f = new File(tmpdir, "storage");
        var raf = new RandomAccessFile(f, "rw");
        storage = new Storage(raf);
        testee = new Allocator(storage);
    }

    @AfterEach
    void tearDown() throws IOException {
        storage.close();
    }

    @Test
    @Disabled("not yet implemented")
    void alloc() {
        var block = testee.alloc(BlockType.SUPER, 8);
        var name = testee.alloc(BlockType.NAME, 15);
    }
}
