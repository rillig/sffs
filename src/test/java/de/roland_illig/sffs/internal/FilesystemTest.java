package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemTest {

    @Test
    void init(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        var fs = new Filesystem(f, "rw");
        fs.close();

        SffsTestUtil.assertDumpEquals(f,
                // superblock
                "53 46 30 31 00 00 00 10  00 00 00 00 00 00 00 02",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                // root directory
                "53 46 64 69 00 00 00 48  00 00 00 00 00 00 00 02",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00"
        );

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 0",
                "block 2 type DIRECTORY size 72",
                "    parent 2"
        );
    }

    @Test
    void mkdir_in_root(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("Downloads"));
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 0",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 7 object 9",
                "block 7 type NAME size 9",
                "    Downloads",
                "block 9 type DIRECTORY size 72",
                "    parent 2"
        );
    }

    @Test
    void mkdir_existing(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("Downloads"));
            assertThatThrownBy(() -> fs.mkdir(Path.of("Downloads")))
                    .isExactlyInstanceOf(FileAlreadyExistsException.class)
                    .hasMessage("Downloads");
        }

        // No traces of trying to create the directory.
        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 0",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 7 object 9",
                "block 7 type NAME size 9",
                "    Downloads",
                "block 9 type DIRECTORY size 72",
                "    parent 2"
        );
    }

    @Test
    void mkdir_in_subdirectory(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("Downloads"));
            fs.mkdir(Path.of("Downloads", "2021"));
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 0",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 7 object 9",
                "block 7 type NAME size 9",
                "    Downloads",
                "block 9 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 14 object 15",
                "block 14 type NAME size 4",
                "    2021",
                "block 15 type DIRECTORY size 72",
                "    parent 9"
        );
    }

    /**
     * Create several directories, so that the directory block needs to be resized.
     */
    @Test
    void mkdir_extend(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("dir1"));
            fs.mkdir(Path.of("dir2"));
            fs.mkdir(Path.of("dir3"));
            fs.mkdir(Path.of("dir4")); // still fits

            fs.mkdir(Path.of("dir5")); // enlarge
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 31 firstFree 2",
                "block 2 type FREE size 72",
                "    nextFree 0",
                "block 7 type NAME size 4",
                "    dir1",
                "block 8 type DIRECTORY size 72",
                "    parent 31",
                "block 13 type NAME size 4",
                "    dir2",
                "block 14 type DIRECTORY size 72",
                "    parent 31",
                "block 19 type NAME size 4",
                "    dir3",
                "block 20 type DIRECTORY size 72",
                "    parent 31",
                "block 25 type NAME size 4",
                "    dir4",
                "block 26 type DIRECTORY size 72",
                "    parent 31",
                "block 31 type DIRECTORY size 136",
                "    parent 31",
                "    entry 0 name 7 object 8",
                "    entry 1 name 13 object 14",
                "    entry 2 name 19 object 20",
                "    entry 3 name 25 object 26",
                "    entry 4 name 40 object 41",
                "block 40 type NAME size 4",
                "    dir5",
                "block 41 type DIRECTORY size 72",
                "    parent 31"
        );
    }

    @Test
    void rmdir(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("Downloads"));
            fs.mkdir(Path.of("Downloads", "2021"));

            assertThatThrownBy(() -> fs.rmdir(Path.of("nonexistent")))
                    .isExactlyInstanceOf(FileNotFoundException.class)
                    .hasMessage("nonexistent");

            assertThatThrownBy(() -> fs.rmdir(Path.of("Downloads")))
                    .isExactlyInstanceOf(DirectoryNotEmptyException.class)
                    .hasMessage("Downloads");

            fs.rmdir(Path.of("Downloads", "2021"));

            fs.rmdir(Path.of("Downloads"));
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 9",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "block 7 type FREE size 9",
                "    nextFree 14",
                "block 9 type FREE size 72",
                "    nextFree 7",
                // TODO: truncate the container file
                "block 14 type FREE size 4",
                "    nextFree 0"
        );
    }

    @Test
    void rename(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("Downloads"));
            fs.mkdir(Path.of("Downloads", "2021"));

            assertThatThrownBy(() -> fs.rename(Path.of("nonexistent"), "new name"))
                    .isExactlyInstanceOf(FileNotFoundException.class)
                    .hasMessage("nonexistent");

            assertThatThrownBy(() -> fs.rename(Path.of("Downloads"), "Downloads"))
                    .isExactlyInstanceOf(FileAlreadyExistsException.class)
                    .hasMessage("Downloads");

            fs.rename(Path.of("Downloads", "2021"), "new name");

            fs.rename(Path.of("Downloads"), "Downloads (archived 2021)");
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 7",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 21 object 9",
                "block 7 type FREE size 9",
                "    nextFree 14",
                "block 9 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 20 object 15",
                "block 14 type FREE size 4",
                "    nextFree 0",
                "block 15 type DIRECTORY size 72",
                "    parent 9",
                "block 20 type NAME size 8",
                "    new name",
                "block 21 type NAME size 25",
                "    Downloads (archived 2021)"
        );
    }

    @Test
    void open_write_close(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            var digits = "12345678".getBytes(StandardCharsets.UTF_8);
            var file = fs.open(Path.of("file"), "w");
            file.write(digits, 0, digits.length);

            file.seek(32000);
            // TODO: resizing a write-only file is not yet implemented
            assertThatThrownBy(() -> file.write(digits, 2, 4))
                    .isExactlyInstanceOf(IndexOutOfBoundsException.class)
                    .hasMessageEndingWith(": 32004");

            file.seek(0x040E);
            file.write(digits, 1, 6);

            file.close();
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 0",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 7 object 8",
                "block 7 type NAME size 4",
                "    file",
                "block 8 type REGULAR size 4096",
                "    size 1044",
                "    00000000  31 32 33 34 35 36 37 38  00 00 00 00 00 00 00 00",
                "    00000400  00 00 00 00 00 00 00 00  00 00 00 00 00 00 32 33",
                "    00000410  34 35 36 37"
        );
    }
}
