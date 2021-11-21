package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlockTest {

    @Test
    void readFully(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");
        BlockTestUtil.dump(f,
                // superblock
                "53 46 30 31 00 00 00 10  00 00 00 00 00 00 00 04",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                // name "Downloads", not used anywhere
                "53 46 6E 6D 00 00 00 09  44 6F 77 6E 6C 6F 61 64",
                "73 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                // root directory, empty
                "53 46 64 69 00 00 00 08  00 00 00 00 00 00 00 04"
        );

        var raf = new RandomAccessFile(f, "rw");
        try (var storage = new Storage(raf)) {
            var block = new Block(storage, 32);
            var buf = new byte[8];
            block.readFully(0, buf, 0, 4);

            assertThat(SffsTestUtil.hexdump(buf, 0, 4)).isEqualTo("44 6F 77 6E");

            // readFully must not read further.
            assertThat(SffsTestUtil.hexdump(buf, 4, 4)).isEqualTo("00 00 00 00");
        }
    }
}
