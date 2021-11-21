package de.roland_illig.sffs;

/**
 * Arithmetics on unsigned integers.
 */
class U {
    static int plus(int a, int b) {
        return a + b;
    }

    static long plus(long a, int b) {
        return a + extend(b);
    }

    private static long extend(int a) {
        return (long) a & 0xFFFF_FFFFL;
    }

    static boolean le(int a, int b) {
        return (a ^ 0x8000_0000) <= (b ^ 0x8000_0000);
    }
}
