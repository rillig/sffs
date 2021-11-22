package de.roland_illig.sffs.internal;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

final class Directory {

    final Block block;

    Directory(Block block) throws IOException {
        this.block = block.checkType(BlockType.DIRECTORY);
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
            // TODO: create new directory with more space
            // TODO: copy all existing entries over to the new directory
            // TODO: update all references to this directory to point to the new directory
            // TODO: - this.parent.find(this.name)
            // TODO: - this.child.parent
            // TODO: change the type of this block to FREE
            // TODO: try again in the new directory
            throw new UnsupportedOperationException("enlarging a directory");
        }

        var nameBlock = block.getStorage().allocName(name);
        var dirBlock = block.getStorage().allocDirectory(block.getRef());
        block.writeBlockRef(firstEmpty, nameBlock.getBlock());
        block.writeBlockRef(firstEmpty + 8, dirBlock.block);
    }

    public Directory lookupDir(String name) throws IOException {
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
}
