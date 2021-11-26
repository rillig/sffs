package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;

public class Api {
    public static de.roland_illig.sffs.Filesystem open(File file, String mode) throws IOException {
        return new Filesystem(file, mode);
    }
}
