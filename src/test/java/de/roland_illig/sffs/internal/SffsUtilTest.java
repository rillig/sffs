package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SffsUtilTest {

    @Test
    void blockEnd() {
        assertThat(SffsUtil.blockEnd(0L, 0)).isEqualTo(16);
        assertThat(SffsUtil.blockEnd(0L, 8)).isEqualTo(16);
        assertThat(SffsUtil.blockEnd(0L, 9)).isEqualTo(32);
        assertThat(SffsUtil.blockEnd(0L, 24)).isEqualTo(32);
        assertThat(SffsUtil.blockEnd(0L, 25)).isEqualTo(48);
    }
}
