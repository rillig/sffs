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
        return block.readLong(0);
    }

    void setParentRef(long ref) throws IOException {
        block.writeLong(0, ref);
    }

    static void init(StorageWriter wr, long parentRef) throws IOException {
        var entries = 4;
        wr.writeInt(BlockType.DIRECTORY.getMagic());
        wr.writeInt(8 + 16 * entries);
        wr.writeLong(parentRef);
        for (var i = 0; i < entries; i++) {
            wr.writeLong(0); // name
            wr.writeLong(0); // object
        }
        wr.writePadding();
    }

    void mkdir(Path dir) throws IOException {
        var name = dir.getFileName().toString();
        var rd = new BlockReader(block, 8); // skip the parent directory
        var firstEmpty = -1;
        while (rd.hasNext()) {
            var nameRef = rd.readRef();
            if (nameRef == 0 && firstEmpty == -1)
                firstEmpty = rd.getPos() - 8;
            if (nameRef != 0 && new Name(block.ref(nameRef)).get().equals(name))
                throw new FileAlreadyExistsException(dir.toString());
            rd.readRef(); // skip the filesystem object
        }

        if (firstEmpty == -1) {
            var enlarged = enlarge();
            enlarged.mkdir(dir);
            return;
        }

        var nameBlock = block.getStorage().allocateName(name);
        var dirBlock = block.getStorage().allocateDirectory(4, block.getRef());
        block.writeRef(firstEmpty, nameBlock.getBlock());
        block.writeRef(firstEmpty + 8, dirBlock.block);
    }

    Directory lookupDir(String name) throws IOException {
        var rd = new BlockReader(block, 8); // skip the parent directory
        while (rd.hasNext()) {
            var nameRef = rd.readRef();
            var objRef = rd.readRef();
            if (nameRef == 0)
                continue;
            var nameInDir = new Name(block.ref(nameRef)).get();
            if (name.equals(nameInDir))
                return new Directory(block.ref(objRef));
        }
        return null;
    }

    private Directory enlarge() throws IOException {
        var large = block.getStorage().allocateDirectory(2 * (block.getSize() / 16), getParentRef());

        var entries = new byte[block.getSize() - 8];
        block.readFully(8, entries, 0, entries.length);
        large.block.write(8, entries, 0, entries.length);

        var parentDir = new Directory(block.ref(getParentRef()));
        for (var pos = 8; pos < parentDir.block.getSize(); pos += 16) {
            if (parentDir.block.readRef(pos + 8) == block.getRef())
                parentDir.block.writeRef(pos + 8, large.block);
        }

        for (var pos = 8; pos < block.getSize(); pos += 16) {
            var childDir = new Directory(block.ref(block.readRef(pos + 8)));
            childDir.block.writeRef(0, large.block);
        }

        var superblock = new Superblock(block.ref(0));
        if (superblock.getRootDirectoryRef() == block.getRef()) {
            superblock.setRootDirectoryRef(large.block);
            large.setParentRef(large.block.getRef());
        }

        block.free();
        return large;
    }
}
