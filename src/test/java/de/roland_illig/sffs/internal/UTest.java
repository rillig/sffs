package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UTest {

    @Test
    void plus_long_int() {
        assertThat(U.plus(0L, 0xFFFF_FFFF)).isEqualTo(0xFFFF_FFFFL);
    }
}
