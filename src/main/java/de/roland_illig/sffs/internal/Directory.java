package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

final class Directory {

    final Block block;

    Directory(Block block) throws IOException {
        this.block = block.checkType(BlockType.DIRECTORY);
    }

    long getParentRef() throws IOException {
        return block.readRef(0);
    }

    static void init(StorageWriter wr, long parentRef) throws IOException {
        var entries = 4;
        wr.writeInt(BlockType.DIRECTORY.getMagic());
        wr.writeInt(8 + 16 * entries);
        wr.writeRef(parentRef);
        for (var i = 0; i < entries; i++) {
            wr.writeRef(0); // name
            wr.writeRef(0); // object
        }
        wr.writePadding();
    }

    void mkdir(Path dir) throws IOException {
        var name = dir.getFileName().toString();
        var firstEmpty = -1;
        for (int pos = 8, size = block.getSize(); pos < size; pos += 16) {
            var nameRef = block.readRef(pos);
            if (nameRef == 0 && firstEmpty == -1)
                firstEmpty = pos;
            if (nameEquals(nameRef, name))
                throw new FileAlreadyExistsException(dir.toString());
        }

        if (firstEmpty == -1) {
            var enlarged = enlarge();
            enlarged.mkdir(dir);
            return;
        }

        var nameBlock = block.getStorage().allocateName(name);
        var dirBlock = block.getStorage().allocateDirectory(4, block.getRef());
        block.writeRef(firstEmpty, nameBlock);
        block.writeRef(firstEmpty + 8, dirBlock);
    }

    Directory lookupDir(String name) throws IOException {
        for (int pos = 8, size = block.getSize(); pos < size; pos += 16) {
            var nameRef = block.readRef(pos);
            if (nameEquals(nameRef, name))
                return new Directory(block.ref(block.readRef(pos + 8)));
        }
        return null;
    }

    private Directory enlarge() throws IOException {
        var large = block.getStorage().allocateDirectory(2 * (block.getSize() / 16), getParentRef());

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

        var superblock = new Superblock(block.ref(0));
        if (superblock.getRootDirectoryRef() == block.getRef()) {
            superblock.setRootDirectoryRef(large);
            large.writeRef(0, large);
        }

        block.free();
        return new Directory(large);
    }

    private boolean nameEquals(long nameRef, String name) throws IOException {
        return nameRef != 0 && new Name(block.ref(nameRef)).get().equals(name);
    }
}
