package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

class SffsTestUtil {

    static String hexdump(byte[] arr) {
        return hexdump(arr, 0, arr.length);
    }

    static String hexdump(byte[] arr, int offset, int length) {
        var sb = new StringBuilder(3 * length);
        for (var i = 0; i < length; i++) {
            if (i > 0) sb.append(' ');
            if (i > 0 && i % 8 == 0) sb.append(' ');
            sb.append(hex(arr[offset + i] >>> 4 & 0x0F));
            sb.append(hex(arr[offset + i] & 0x0F));
        }
        return sb.toString();
    }

    static byte[] fromHexdump(String hex) {
        var baos = new ByteArrayOutputStream();
        for (int i = 0, len = hex.length(); i < len; ) {
            while (i < len && hex.charAt(i) == ' ')
                i++;
            var hi = unhex(hex.charAt(i++));
            var lo = unhex(hex.charAt(i++));
            baos.write(16 * hi + lo);
        }
        return baos.toByteArray();
    }

    private static char hex(int i) {
        return "0123456789ABCDEF".charAt(i);
    }

    private static int unhex(char ch) {
        var v = Character.digit(ch, 16);
        assertThat(v).isNotEqualTo(-1);
        return v;
    }

    static void assertDumpEquals(File f, String... expectedHexRows) throws IOException {
        var actual = new ArrayList<String>();
        try (var fis = new FileInputStream(f)) {
            var bis = new BufferedInputStream(fis);
            var bytes = new byte[16];
            int n;
            while ((n = bis.read(bytes, 0, 16)) != -1) {
                assertThat(n).isEqualTo(16);
                actual.add(hexdump(bytes));
            }
        }
        assertThat(actual).containsExactly(expectedHexRows);
    }

    static void dump(File f, String... hexRows) throws IOException {
        try (var fos = new FileOutputStream(f)) {
            try (var bos = new BufferedOutputStream(fos)) {
                for (var row : hexRows) bos.write(fromHexdump(row));
            }
        }
    }

    static void assertTextDumpEquals(File f, String... expectedTextRows) throws IOException {
        var actual = Dumper.dump(f);
        assertThat(actual).containsExactly(expectedTextRows);
    }

    static void assertTextDumpEquals(OpenFile f, String... expectedTextRows) throws IOException {
        var lines = new ArrayList<String>();

        var zero = new byte[16];
        var row = new byte[16];
        var off = 0L;
        int n;
        for (; (n = f.read(row, 0, row.length)) > 0; off += n)
            if (!Arrays.equals(row, 0, n, zero, 0, n))
                lines.add(String.format(Locale.ROOT, "0x%08x  %s", off, SffsTestUtil.hexdump(row, 0, n)));
        lines.add(String.format(Locale.ROOT, "size 0x%08x", off));

        assertThat(lines).containsExactly(expectedTextRows);
    }
}
