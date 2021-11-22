package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SffsUtilTest {

    @Test
    void checkRange() {
        var min = Integer.MIN_VALUE;
        var max = Integer.MAX_VALUE;

        checkRangeBad(-1, 0, 0, -1);
        checkRangeBad(0, -1, 0, -1);
        checkRangeBad(0, 0, -1, -1);

        checkRangeGood(0, 0, 0);
        checkRangeGood(0, 0, max);
        checkRangeBad(0, 1, 0, 1);
        checkRangeGood(0, 1, 1);
        checkRangeBad(1, 1, 1, 2);
        checkRangeGood(1, 4, 5);
        checkRangeBad(1, 5, 5, 6);

        checkRangeGood(max, 0, max);
        checkRangeBad(max, 1, max, min);
        checkRangeBad(1, max, max, min);
    }

    private static void checkRangeGood(int off, int len, int size) {
        assertThatCode(() -> SffsUtil.checkRange(off, len, size))
                .doesNotThrowAnyException();
    }

    private static void checkRangeBad(int off, int len, int size, int errorIndex) {
        assertThatThrownBy(() -> SffsUtil.checkRange(off, len, size))
                .isExactlyInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageEndingWith(": " + errorIndex);
    }

    @Test
    void blockEnd() {
        assertThat(SffsUtil.blockEnd(0L, 0)).isEqualTo(16);
        assertThat(SffsUtil.blockEnd(0L, 8)).isEqualTo(16);
        assertThat(SffsUtil.blockEnd(0L, 9)).isEqualTo(32);
        assertThat(SffsUtil.blockEnd(0L, 24)).isEqualTo(32);
        assertThat(SffsUtil.blockEnd(0L, 25)).isEqualTo(48);
    }
}
