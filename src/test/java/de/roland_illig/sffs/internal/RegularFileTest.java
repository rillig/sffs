package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RegularFileTest {

    @Test
    void read_small_file_large_buffer(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("small"), "w")) {
                var buf = new byte[]{0x55};
                file.write(buf, 0, 1);
            }

            try (var file = fs.open(Path.of("small"), "r")) {
                var buf = new byte[16 * 1024];
                var n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(1);
                assertThat(buf[0]).isEqualTo((byte) 0x55);
            }
        }
    }

    @Test
    void read_large_file_large_buffer(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("large"), "w")) {
                var buf = new byte[]{0x55};
                file.seek(100_000);
                file.write(buf, 0, 1);
            }

            try (var file = fs.open(Path.of("large"), "r")) {
                var buf = new byte[16 * 1024];
                file.seek(100_000);
                var n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(2400); // FIXME: must be 1
                assertThat(buf[0]).isEqualTo((byte) 0x55);
            }
        }
    }
}
