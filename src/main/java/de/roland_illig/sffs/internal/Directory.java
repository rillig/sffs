package de.roland_illig.sffs.internal;

import java.io.IOException;

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

    void mkdir(String name) throws IOException {
        var rd = new BlockReader(block, 8); // skip the parent directory
        while (rd.hasNext()) {
            var nameRef = rd.readRef();
            if (nameRef == 0) {
                var nameBlock = block.getStorage().allocName(name);
                var dirBlock = block.getStorage().allocDirectory(block.getRef());
                block.writeBlockRef(rd.getPos() - 8, nameBlock.getBlock());
                block.writeBlockRef(rd.getPos(), dirBlock.block);
                return;
            }
            rd.readRef(); // skip the filesystem object
        }
        throw new UnsupportedOperationException("enlarging a directory");
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
