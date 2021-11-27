package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
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
                file.seek(5_000);
                file.write(new byte[]{'a'}, 0, 1);
                file.seek(15_000);
                file.write(new byte[]{'b'}, 0, 1);
                file.seek(65536);
                file.write(new byte[]{'c'}, 0, 1);
            }

            try (var file = fs.open(Path.of("large"), "r")) {
                var buf = new byte[17 * 1024]; // not a multiple of the chunk size 4096
                var n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(buf.length);
                assertThat(SffsTestUtil.hexdumpBrief(buf)).containsExactly(
                        "00001380  00 00 00 00 00 00 00 00  61 00 00 00 00 00 00 00",
                        "00003a90  00 00 00 00 00 00 00 00  62 00 00 00 00 00 00 00"
                );

                n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(buf.length);
                assertThat(SffsTestUtil.hexdumpBrief(buf)).containsExactly(
                        // FIXME: Data from null chunks must be overwritten with zero
                        "00001380  00 00 00 00 00 00 00 00  61 00 00 00 00 00 00 00",
                        "00003a90  00 00 00 00 00 00 00 00  62 00 00 00 00 00 00 00"
                );

                n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(buf.length);
                assertThat(SffsTestUtil.hexdumpBrief(buf)).containsExactly(
                        // FIXME: Data from null chunks must be overwritten with zero
                        "00001380  00 00 00 00 00 00 00 00  61 00 00 00 00 00 00 00",
                        "00003a90  00 00 00 00 00 00 00 00  62 00 00 00 00 00 00 00"
                );

                n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(13313);
                assertThat(SffsTestUtil.hexdumpBrief(Arrays.copyOf(buf, 13313))).containsExactly(
                        // FIXME: Data from null chunks must be overwritten with zero
                        "00001380  00 00 00 00 00 00 00 00  61 00 00 00 00 00 00 00",
                        "00003400  63"
                );
            }
        }
    }

    @Test
    void read_large_file_large_buffer_at_eof(@TempDir File tmpdir) throws IOException {
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
                assertThat(n).isEqualTo(1);
                assertThat(buf[0]).isEqualTo((byte) 0x55);
            }
        }
    }
}
