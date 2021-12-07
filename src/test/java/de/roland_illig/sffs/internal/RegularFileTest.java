package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                        /* only 00 bytes */
                );

                n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(buf.length);
                assertThat(SffsTestUtil.hexdumpBrief(buf)).containsExactly(
                        /* only 00 bytes */
                );

                n = file.read(buf, 0, buf.length);
                assertThat(n).isEqualTo(13313);
                assertThat(SffsTestUtil.hexdumpBrief(Arrays.copyOf(buf, 13313))).containsExactly(
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

    @Test
    void read_errors(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("large"), "w")) {
                var buf = new byte[]{0x55};
                file.seek(100_000);
                file.write(buf, 0, 1);
            }

            try (var file = fs.open(Path.of("large"), "r")) {
                file.seek(-1);
                assertThatThrownBy(() -> file.read(new byte[1], 0, 1))
                        .isInstanceOf(IndexOutOfBoundsException.class)
                        .hasMessageEndingWith(": -1");

                file.seek(Integer.MAX_VALUE - 25);
                assertThat(file.read(new byte[1], 0, 1)).isEqualTo(-1);

                // XXX: It feels somewhat arbitrary to limit the file offset to 24 below
                //  Integer.MAX_VALUE. Why exactly 24, from an API point of view?
                file.seek(Integer.MAX_VALUE - 24);
                assertThatThrownBy(() -> file.read(new byte[1], 0, 1))
                        .isInstanceOf(IndexOutOfBoundsException.class)
                        .hasMessageEndingWith(": 2147483624");
            }
        }
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Test
    void read_zero_bytes(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("large"), "w")) {
                var buf = new byte[]{0x55};
                file.seek(100_000);
                file.write(buf, 0, 1);
            }

            try (var file = fs.open(Path.of("large"), "r")) {
                assertThat(file.read(new byte[1], 0, 0)).isEqualTo(0);
            }
        }
    }

    /**
     * This test demonstrates that when readLarge is called, the expected length to be read is already trimmed down
     * to the remaining size of the file, therefore there can be no short reads in readLarge.
     */
    @Test
    void readLarge_first_chunk_short(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("large"), "w")) {
                file.seek(8192);
                file.write(new byte[]{'U'}, 0, 1);
            }

            try (var file = fs.open(Path.of("large"), "r")) {
                var buf = new byte[8192];
                buf[1] = 'Z';
                file.seek(8192);

                var n = file.read(buf, 0, buf.length);

                assertThat(n).isEqualTo(1);
                assertThat(buf[0]).isEqualTo((byte) 'U');
                assertThat(buf[1]).isEqualTo((byte) 'Z');
            }
        }
    }

    /**
     * This test demonstrates that when readLarge is called, the expected length to be read is already trimmed down
     * to the remaining size of the file, therefore there can be no short reads in readLarge.
     */
    @Test
    void write_large_buffer_to_large_file(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("large"), "w")) {
                var buf = new byte[17 * 1024];
                buf[0] = 'a';
                buf[buf.length - 1] = 'z';

                file.seek(8000);
                file.write(buf, 0, buf.length);
            }

            try (var file = fs.open(Path.of("large"), "r")) {
                SffsTestUtil.assertTextDumpEquals(file,
                        "0x00001f40  61 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                        "0x00006330  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 7A",
                        "size 0x00006340"
                );
            }
        }
    }

    @Test
    void write_errors(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("large"), "w")) {

                file.seek(-1);
                assertThatThrownBy(() -> file.write(new byte[1], 0, 1))
                        .isInstanceOf(IndexOutOfBoundsException.class)
                        .hasMessageEndingWith(": -1");

                // This limit is due to an incomplete implementation of large files,
                // it is not a conceptual limit.
                file.seek(Integer.MAX_VALUE - 25);
                assertThatThrownBy(() -> file.write(new byte[1], 0, 1))
                        .isInstanceOf(IndexOutOfBoundsException.class)
                        .hasMessageEndingWith(": 4194328");

                // XXX: It feels somewhat arbitrary to limit the file offset to 24 below
                //  Integer.MAX_VALUE. Why exactly 24, from an API point of view?
                file.seek(Integer.MAX_VALUE - 24);
                assertThatThrownBy(() -> file.write(new byte[1], 0, 1))
                        .isInstanceOf(IndexOutOfBoundsException.class)
                        .hasMessageEndingWith(": 2147483624");
            }
        }
    }

    /**
     * Opening a file in write mode truncates it.
     */
    @Test
    void write_after_read(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("file"), "w")) {
                var buf = new byte[1000];
                file.write(buf, 0, buf.length);
            }

            try (var file = fs.open(Path.of("file"), "r")) {
                var buf = new byte[1001];
                assertThat(file.read(buf, 0, buf.length)).isEqualTo(1000);
            }

            fs.open(Path.of("large"), "w").close();

            try (var file = fs.open(Path.of("file"), "r")) {
                var buf = new byte[1001];
                // FIXME: must be truncated to 0
                assertThat(file.read(buf, 0, buf.length)).isEqualTo(1000);
            }

            try (var file = fs.open(Path.of("file"), "w")) {
                var buf = new byte[1];
                file.write(buf, 0, buf.length);
            }

            try (var file = fs.open(Path.of("file"), "r")) {
                var buf = new byte[1001];
                // FIXME: must be truncated to 1
                assertThat(file.read(buf, 0, buf.length)).isEqualTo(1000);
            }
        }
    }
}
