package de.roland_illig.sffs.internal;

final class SffsUtil {

    static void require(boolean cond) {
        if (!cond) throw new IllegalArgumentException();
    }
}
