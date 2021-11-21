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

    static boolean le(int a, int b) {
        return Integer.compareUnsigned(a, b) <= 0;
    }

    private static long extend(int a) {
        return (long) a & 0xFFFF_FFFFL;
    }
}
