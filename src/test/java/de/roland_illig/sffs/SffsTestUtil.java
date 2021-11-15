package de.roland_illig.sffs;

class SffsTestUtil {

    static String hex(byte[] arr) {
        return hex(arr, 0, arr.length);
    }

    static String hex(byte[] arr, int offset, int length) {
        char[] hex = new char[2 * length];
        for (int i = 0; i < length; i++) {
            hex[2 * i] = hex(arr[offset + i] >>> 4 & 0x0F);
            hex[2 * i + 1] = hex(arr[offset + i] & 0x0F);
        }
        return new String(hex);
    }

    private static char hex(int i) {
        return "0123456789ABCDEF".charAt(i);
    }
}
