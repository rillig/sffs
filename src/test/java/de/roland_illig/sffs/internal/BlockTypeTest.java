package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class BlockTypeTest {

    /**
     * Ensure that the magic number of the superblock reveals whether the data is stored in little endian or big
     * endian, just in case that a future extension may require little endian storage.
     */
    @Test
    void super_magic() {
        var magic = BlockType.SUPER.getMagic();
        var swapped = Integer.reverseBytes(magic);

        assertThat(magic).isNotEqualTo(swapped);
    }

    /**
     * For consistency, internal data inconsistencies are reported as IOException as well.
     */
    @Test
    void byMagic_invalid() {
        assertThatThrownBy(() -> BlockType.byMagic(0))
                .isInstanceOf(IOException.class);
    }
}
