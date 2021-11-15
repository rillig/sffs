package de.roland_illig.sffs;

class SffsUtil {
    public static void require(boolean cond) {
        if (!cond) throw new IllegalArgumentException();
    }

    public static boolean ule(int a, int b) {
        return (a ^ 0x8000_0000) <= (b ^ 0x8000_0000);
    }

    public static long uplus(long a, int b) {
        return a + ((long) b & 0xFFFF_FFFFL);
    }
}
