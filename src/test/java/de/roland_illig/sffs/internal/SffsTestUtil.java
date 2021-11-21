package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;

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
}
