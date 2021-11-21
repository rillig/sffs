package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class BlockTestUtil {

    static void assertDumpEquals(File f, String... expected) throws IOException {
        var actual = new ArrayList<String>();
        try (var fis = new FileInputStream(f)) {
            var bis = new BufferedInputStream(fis);
            var bytes = new byte[16];
            int n;
            while ((n = bis.read(bytes, 0, 16)) != -1) {
                assertThat(n).isEqualTo(16);
                actual.add(SffsTestUtil.hexdump(bytes));
            }
        }
        assertThat(actual).containsExactly(expected);
    }

    static void dump(File f, String... rows) throws IOException {
        try (var fos = new FileOutputStream(f)) {
            try (var bos = new BufferedOutputStream(fos)) {
                for (var row : rows) bos.write(SffsTestUtil.fromHexdump(row));
            }
        }
    }
}
