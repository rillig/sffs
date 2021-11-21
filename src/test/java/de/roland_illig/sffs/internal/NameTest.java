package de.roland_illig.sffs.internal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class NameTest {

    @Test
    void check_string_empty() {
        assertThatThrownBy(() -> Name.check(""))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("Invalid name \"\"");
    }

    @Test
    void check_string_dot() {
        assertThatThrownBy(() -> Name.check("."))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("Invalid name \".\"");
    }

    @Test
    void check_string_dot_dot() {
        assertThatThrownBy(() -> Name.check(".."))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("Invalid name \"..\"");
    }

    @Test
    void check_string_dot_dot_dot() {
        assertThatCode(() -> Name.check("..."))
                .doesNotThrowAnyException();
    }

    @Test
    void check_char_null() {
        assertThatThrownBy(() -> Name.check("Down\0loads"))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("Invalid character U+0000");
    }

    @Test
    void check_char_solidus() {
        assertThatThrownBy(() -> Name.check("Downloads/old"))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("Invalid character U+002F");
    }

    @Test
    void check_char_reverse_solidus() {
        assertThatThrownBy(() -> Name.check("Downloads\\old"))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("Invalid character U+005C");
    }
}
