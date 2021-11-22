package de.roland_illig.sffs.internal;

/**
 * Arithmetics on unsigned integers.
 */
final class U {

    static int plus(int a, int b) {
        return a + b;
    }

    static long plus(long a, int b) {
        return a + extend(b);
    }

    private static long extend(int a) {
        return (long) a & 0xFFFF_FFFFL;
    }
}
