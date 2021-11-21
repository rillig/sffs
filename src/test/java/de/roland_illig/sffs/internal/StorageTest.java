package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageTest {

    @Test
    void init(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");
        var raf = new RandomAccessFile(f, "rw");

        var storage = new Storage(raf);
        storage.close();

        SffsTestUtil.assertDumpEquals(f,
                // superblock
                "53 46 30 31 00 00 00 10  00 00 00 00 00 00 00 02",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                // root directory
                "53 46 64 69 00 00 00 48  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00"
        );
    }
}
