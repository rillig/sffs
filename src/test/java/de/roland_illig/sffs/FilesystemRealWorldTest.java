package de.roland_illig.sffs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemRealWorldTest {

    @Test
    void copyTrees(@TempDir File tmpdir) throws IOException {
        var storage = new File(tmpdir, "storage");

        try (var fs = Filesystem.open(storage, "rw")) {
            Files.walkFileTree(Path.of("."), new CopyIn(fs));
        }

        try (var fs = Filesystem.open(storage, "rw")) {
            Files.walkFileTree(Path.of("."), new Verify(fs));
        }
    }

    static class CopyIn extends SimpleFileVisitor<Path> {
        private final Filesystem fs;
        private final byte[] buf = new byte[4096];

        CopyIn(Filesystem fs) {
            this.fs = fs;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            var result = skipHidden(dir);
            if (result == FileVisitResult.CONTINUE && !dir.equals(Path.of(".")))
                fs.mkdir(dir);
            return result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try (var in = new FileInputStream(file.toFile())) {
                try (var out = fs.open(file, "w")) {
                    int n;
                    while ((n = in.read(buf, 0, buf.length)) > 0)
                        out.write(buf, 0, n);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }

    static class Verify extends SimpleFileVisitor<Path> {
        private final Filesystem fs;
        private final byte[] outerBuf = new byte[4096];
        private final byte[] innerBuf = new byte[4096];

        Verify(Filesystem fs) {
            this.fs = fs;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return skipHidden(dir);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            assertFilesEqual(file);
            return FileVisitResult.CONTINUE;
        }

        private void assertFilesEqual(Path file) throws IOException {
            try (var outer = new FileInputStream(file.toFile())) {
                try (var inner = fs.open(file, "r")) {
                    while (true) {
                        int nOuter, nInner;
                        nOuter = outer.read(outerBuf, 0, outerBuf.length);
                        nInner = inner.read(innerBuf, 0, innerBuf.length);
                        assertThat(nInner).isEqualTo(nOuter);
                        if (nOuter <= 0)
                            break;
                        assertThat(Arrays.copyOf(outerBuf, nOuter)).isEqualTo(Arrays.copyOf(innerBuf, nInner));
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalStateException(file.toString(), e);
            }
        }
    }

    private static FileVisitResult skipHidden(Path dir) {
        // skip hidden directories, especially .git, since that might contain files over 2 MB.
        var name = dir.getFileName().toString();
        if (name.equals("."))
            return FileVisitResult.CONTINUE;
        if (name.startsWith("."))
            return FileVisitResult.SKIP_SUBTREE;

        /* The directory build/test-results/test/binary gets modified during the test. */
        if (dir.equals(Path.of(".", "build")))
            return FileVisitResult.SKIP_SUBTREE;

        return FileVisitResult.CONTINUE;
    }
}
