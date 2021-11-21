package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemTest {

    @Test
    void mkdir_in_root(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            // TODO: implement this
            assertThatThrownBy(() -> fs.mkdir(Path.of("Downloads")))
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
        }

        // TODO: create the name
        // TODO: create an empty directory
        // TODO: list the new directory in the root directory
        SffsTestUtil.assertDumpEquals(f,
                "53 46 30 31 00 00 00 10  00 00 00 00 00 00 00 01",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "53 46 64 69 00 00 00 48  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00"
        );
    }

    @Test
    void mkdir_in_subdirectory(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            // TODO: implement this
            assertThatThrownBy(() -> fs.mkdir(Path.of("Downloads")))
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
            // TODO: mkdir("Downloads/2021")
        }

        // TODO: create the name
        // TODO: create the directory "Downloads"
        // TODO: create the subdirectory "2021"
        // TODO: list the directory "2021" in "Downloads"
        // TODO: list the directory "Downloads" in the root directory
        SffsTestUtil.assertDumpEquals(f,
                "53 46 30 31 00 00 00 10  00 00 00 00 00 00 00 01",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "53 46 64 69 00 00 00 48  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00",
                "00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00"
        );
    }
}
