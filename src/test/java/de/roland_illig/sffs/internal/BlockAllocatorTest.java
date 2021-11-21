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

class BlockAllocatorTest {

    private Storage storage;
    private BlockAllocator testee;

    @BeforeEach
    void setUp(@TempDir File tmpdir) throws FileNotFoundException {
        var f = new File(tmpdir, "storage");
        var raf = new RandomAccessFile(f, "rw");
        storage = new Storage(raf);
        testee = new BlockAllocator(new Allocator(storage));
    }

    @AfterEach
    void tearDown() throws IOException {
        storage.close();
    }

    @Test
    @Disabled("not yet implemented")
    void allocSuper() {
        var block = testee.allocSuperblock();
    }

    @Test
    @Disabled("not yet implemented")
    void allocName() throws IOException {
        var block = testee.allocName("directory name");
    }
}
