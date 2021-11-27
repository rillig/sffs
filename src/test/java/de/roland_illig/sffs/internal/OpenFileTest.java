package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenFileTest {

    @Test
    void read(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("file"), "w")) {
                assertThatThrownBy(() -> file.read(new byte[1], 0, 1))
                        .isInstanceOf(IOException.class)
                        .hasMessage("write-only");
            }
        }
    }

    @Test
    void write(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("file"), "w")) {
                file.write(new byte[]{1, 2}, 0, 2);
            }

            try (var file = fs.open(Path.of("file"), "r")) {
                assertThatThrownBy(() -> file.write(new byte[1], 0, 1))
                        .isInstanceOf(IOException.class)
                        .hasMessage("read-only");
            }
        }
    }

    @Test
    void tell(@TempDir File tmpdir) throws IOException {
        var f = new File(tmpdir, "storage");

        try (var fs = new Filesystem(f, "rw")) {
            try (var file = fs.open(Path.of("file"), "w")) {
                file.write(new byte[]{1, 2}, 0, 2);
                assertThat(file.tell()).isEqualTo(2);
                file.seek(12345);
                assertThat(file.tell()).isEqualTo(12345);
            }
        }
    }
}
