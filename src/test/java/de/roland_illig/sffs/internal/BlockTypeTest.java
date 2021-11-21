package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BlockTypeTest {

    /**
     * Ensure that the magic number of the superblock reveals whether the data is stored in little endian or big
     * endian, just in case that a future extension may require little endian storage.
     */
    @Test
    void super_magic() {
        int magic = BlockType.SUPER.getMagic();
        int swapped = Integer.reverseBytes(magic);

        assertThat(magic).isNotEqualTo(swapped);
    }
}
