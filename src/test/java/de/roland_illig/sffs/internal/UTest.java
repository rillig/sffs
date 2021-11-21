package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UTest {

    @Test
    void plus_int_int() {
        assertThat(U.plus(0, 0)).isEqualTo(0);
        assertThat(U.plus(0xFFFF_FFFF, 0xFFFF_FFFF)).isEqualTo(0xFFFF_FFFE);
    }

    @Test
    void plus_long_int() {
        assertThat(U.plus(0L, 0xFFFF_FFFF)).isEqualTo(0xFFFF_FFFFL);
    }

    @Test
    void le() {
        assertThat(U.le(0, 0)).isTrue();
        assertThat(U.le(0, 0xFFFF_FFFF)).isTrue();
        assertThat(U.le(0xFFFF_FFFF, 0)).isFalse();
        assertThat(U.le(0x7FFF_FFFF, 0x8000_0000)).isTrue();
    }
}
