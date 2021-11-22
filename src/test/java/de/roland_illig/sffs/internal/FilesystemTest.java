package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
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
            Assertions.assertThatThrownBy(() -> fs.mkdir(Path.of("Downloads")))
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
}
