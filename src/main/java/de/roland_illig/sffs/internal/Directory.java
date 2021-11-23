package de.roland_illig.sffs.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

/**
 * A directory consists of a reference to its parent directory, followed by the directory entries as (name, object).
 */
final class Directory {

    final Block block;

    Directory(Block block) throws IOException {
        this.block = block.checkType(BlockType.DIRECTORY);
    }

    long getParentRef() throws IOException {
        return block.readRef(0);
    }

    void mkdir(Path dir) throws IOException {
        var name = dir.getFileName().toString();
        var emptyPos = -1;
        for (int pos = 8, size = block.getSize(); pos < size; pos += 16) {
            var nameRef = block.readRef(pos);
            if (nameRef == 0 && emptyPos == -1)
                emptyPos = pos;
            if (name.equals(nameAtRef(nameRef)))
                throw new FileAlreadyExistsException(dir.toString());
        }

        if (emptyPos == -1) {
            var enlarged = enlarge();
            enlarged.mkdir(dir);
            return;
        }

        var nameBlock = block.storage.allocateName(name);
        var dirBlock = block.storage.allocateDirectory(4, block.getRef());
        block.writeRef(emptyPos, nameBlock);
        block.writeRef(emptyPos + 8, dirBlock);
    }

    void remove(Path dir) throws IOException {
        for (int pos = 8, size = block.getSize(); pos < size; pos += 16)
            if (block.readRef(pos) != 0)
                throw new DirectoryNotEmptyException(dir.toString());

        var parent = new Directory(block.ref(block.readRef(0)));
        for (int pos = 8, size = parent.block.getSize(); pos < size; pos += 16) {
            if (parent.block.readRef(pos + 8) == block.getRef()) {
                block.ref(parent.block.readRef(pos), BlockType.NAME).free();
                parent.block.writeRef(pos, 0);
                parent.block.writeRef(pos + 8, 0);
                block.free();
                return;
            }
        }
    }

    void rename(Path oldPath, String newName) throws IOException {
        var oldPos = -1;
        var oldName = oldPath.getFileName().toString();
        for (int pos = 8, size = block.getSize(); pos < size; pos += 16) {
            var name = nameAtPos(pos);
            if (newName.equals(name))
                throw new FileAlreadyExistsException(oldPath.resolveSibling(newName).toString());
            if (oldName.equals(name))
                oldPos = pos;
        }

        if (oldPos == -1)
            throw new FileNotFoundException(oldPath.toString());
        var name = block.storage.allocateName(newName);
        block.ref(block.readRef(oldPos), BlockType.NAME).free();
        block.writeRef(oldPos, name);
    }

    Directory lookupDir(String name) throws IOException {
        for (int pos = 8, size = block.getSize(); pos < size; pos += 16)
            if (name.equals(nameAtPos(pos)))
                return new Directory(block.ref(block.readRef(pos + 8)));
        return null;
    }

    private Directory enlarge() throws IOException {
        var large = block.storage.allocateDirectory(2 * (block.getSize() / 16), getParentRef());

        var entries = new byte[block.getSize() - 8];
        block.readFully(8, entries, 0, entries.length);
        large.write(8, entries, 0, entries.length);

        var parent = block.ref(getParentRef(), BlockType.DIRECTORY);
        for (int pos = 8, size = parent.getSize(); pos < size; pos += 16) {
            if (parent.readRef(pos + 8) == block.getRef())
                parent.writeRef(pos + 8, large);
        }

        for (int pos = 8, size = block.getSize(); pos < size; pos += 16) {
            var childDir = block.ref(block.readRef(pos + 8), BlockType.DIRECTORY);
            childDir.writeRef(0, large);
        }

        var superblock = new Superblock(block.storage);
        if (superblock.getRootDirectoryRef() == block.getRef()) {
            superblock.setRootDirectory(large);
            large.writeRef(0, large);
        }

        block.free();
        return new Directory(large);
    }

    private String nameAtRef(long nameRef) throws IOException {
        return new Name(block.ref(nameRef)).get();
    }

    private String nameAtPos(int pos) throws IOException {
        var nameRef = block.readRef(pos);
        return nameRef != 0 ? new Name(block.ref(nameRef)).get() : null;
    }
}
