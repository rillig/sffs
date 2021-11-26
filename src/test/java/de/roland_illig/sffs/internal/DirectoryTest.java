package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DirectoryTest {

    @Test
    void open_nonexistent(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            assertThatThrownBy(() -> fs.open(Path.of("nonexistent"), "r"))
                    .isInstanceOf(FileNotFoundException.class)
                    .hasMessage("nonexistent");
        }
    }

    @Test
    void move_enlarge(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            fs.mkdir(Path.of("1"));
            fs.mkdir(Path.of("2"));
            fs.mkdir(Path.of("3"));
            fs.mkdir(Path.of("4"));
        }

        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 2 firstFree 0",
                "block 2 type DIRECTORY size 72",
                "    parent 2",
                "    entry 0 name 7 object 8",
                "    entry 1 name 13 object 14",
                "    entry 2 name 19 object 20",
                "    entry 3 name 25 object 26",
                "block 7 type NAME size 1",
                "    1",
                "block 8 type DIRECTORY size 72",
                "    parent 2",
                "block 13 type NAME size 1",
                "    2",
                "block 14 type DIRECTORY size 72",
                "    parent 2",
                "block 19 type NAME size 1",
                "    3",
                "block 20 type DIRECTORY size 72",
                "    parent 2",
                "block 25 type NAME size 1",
                "    4",
                "block 26 type DIRECTORY size 72",
                "    parent 2"
        );

        try (var fs = new Filesystem(f, "rw")) {
            // FIXME
            assertThatThrownBy(() -> fs.move(Path.of("2"), Path.of("5")))
                    .isInstanceOf(WrongTypeException.class)
                    .hasMessage("expected DIRECTORY, got FREE");
        }

        // FIXME: must be the same as before
        SffsTestUtil.assertTextDumpEquals(f,
                "block 0 type SUPER size 16",
                "    root 31 firstFree 13",
                "block 2 type FREE size 72",
                "    nextFree 0",
                "block 7 type NAME size 1",
                "    1",
                "block 8 type DIRECTORY size 72",
                "    parent 31",
                "block 13 type FREE size 1",
                "    nextFree 2",
                "block 14 type DIRECTORY size 72",
                "    parent 31",
                "block 19 type NAME size 1",
                "    3",
                "block 20 type DIRECTORY size 72",
                "    parent 31",
                "block 25 type NAME size 1",
                "    4",
                "block 26 type DIRECTORY size 72",
                "    parent 31",
                "block 31 type DIRECTORY size 136",
                "    parent 31",
                "    entry 0 name 7 object 8",
                "    entry 1 name 13 object 14",
                "    entry 2 name 19 object 20",
                "    entry 3 name 25 object 26",
                "    entry 4 name 40 object 2",
                "block 40 type NAME size 1",
                "    5"
        );
    }
}
