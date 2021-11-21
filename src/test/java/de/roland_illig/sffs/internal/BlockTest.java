package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlockTest {

    @Test
    void readFully(@TempDir File tmpdir) throws IOException {
        var storageFile = new RandomAccessFile(new File(tmpdir, "storage"), "rw");
        storageFile.write("0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));

        try (var storage = new Storage(storageFile)) {
            var block = new Block(storage, 0);
            var buf = new byte[32];
            block.readFully(0, buf, 0, 4);

            assertThat(SffsTestUtil.hex(buf, 0, 4)).isEqualTo("38394142");
            assertThat(SffsTestUtil.hex(buf, 4, 4)).isEqualTo("00000000");
        }
    }
}
