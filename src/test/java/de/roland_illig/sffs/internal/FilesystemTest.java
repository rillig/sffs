package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
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
}
