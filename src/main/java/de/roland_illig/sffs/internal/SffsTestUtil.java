package de.roland_illig.sffs.internal;

class SffsTestUtil {

    static String hex(byte[] arr, int offset, int length) {
        var hex = new char[2 * length];
        for (var i = 0; i < length; i++) {
            hex[2 * i] = hex(arr[offset + i] >>> 4 & 0x0F);
            hex[2 * i + 1] = hex(arr[offset + i] & 0x0F);
        }
        return new String(hex);
    }

    static String hexdump(byte[] arr) {
        var length = arr.length;
        var sb = new StringBuilder(3 * length);
        for (var i = 0; i < length; i++) {
            if (i > 0) sb.append(' ');
            if (i % 8 == 0) sb.append(' ');
            sb.append(hex(arr[i] >>> 4 & 0x0F));
            sb.append(hex(arr[i] & 0x0F));
        }
        return sb.toString();
    }

    private static char hex(int i) {
        return "0123456789ABCDEF".charAt(i);
    }
}
