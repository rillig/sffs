package de.roland_illig.sffs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemRealWorldTest {

    @Test
    void copyTrees(@TempDir File tmpdir) throws IOException {
        var storage = new File(tmpdir, "storage");

        try (var fs = Filesystem.open(storage, "rw")) {
            Files.walkFileTree(Path.of("."), new CopyIn(fs));
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
            dir = dir.normalize();

            // skip hidden files, especially .git, since that might contain files over 2 MB.
            var name = dir.getFileName().toString();
            if (name.equals(""))
                return FileVisitResult.CONTINUE;
            if (name.startsWith("."))
                return FileVisitResult.SKIP_SUBTREE;

            fs.mkdir(dir.normalize());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            file = file.normalize();

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
}
