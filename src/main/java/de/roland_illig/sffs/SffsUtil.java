package de.roland_illig.sffs;

class SffsUtil {
    public static void require(boolean cond) {
        if (!cond) throw new IllegalArgumentException();
    }
}
