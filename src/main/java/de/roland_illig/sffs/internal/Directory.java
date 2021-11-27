package de.roland_illig.sffs.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A directory consists of a reference to its parent directory, followed by the directory entries as (name, object).
 */
final class Directory {

    final Block block;

    Directory(Block block) throws IOException {
        this.block = block.checkType(BlockType.DIRECTORY);
    }

    Directory getParent() throws IOException {
        return new Directory(block.ref(getParentRef()));
    }

    private long getParentRef() throws IOException {
        return block.readRef(0);
    }

    void setParent(Block parent) throws IOException {
        block.writeRef(0, parent.checkType(BlockType.DIRECTORY));
    }

    private int getEntriesCount() throws IOException {
        return (block.getSize() - 8) / 16;
    }

    private long getNameRef(int entry) throws IOException {
        return block.readRef(8 + entry * 16);
    }

    private Block getName(int entry) throws IOException {
        return block.ref(getNameRef(entry));
    }

    private String getNameString(int entry) throws IOException {
        return new Name(getName(entry)).get();
    }

    private void setNameRef(int entry, long ref) throws IOException {
        block.writeRef(8 + entry * 16, ref);
    }

    private void setName(int entry, Block name) throws IOException {
        block.writeRef(8 + entry * 16, name);
    }

    private long getObjectRef(int entry) throws IOException {
        return block.readRef(8 + entry * 16 + 8);
    }

    private Block getObject(int entry) throws IOException {
        return block.ref(getObjectRef(entry));
    }

    private void setObjectRef(int entry, long ref) throws IOException {
        block.writeRef(8 + entry * 16 + 8, ref);
    }

    private void setObject(int entry, Block obj) throws IOException {
        block.writeRef(8 + entry * 16 + 8, obj);
    }

    List<String> readdir() throws IOException {
        var names = new ArrayList<String>();
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            var nameRef = getNameRef(entry);
            if (nameRef != 0)
                names.add(nameAtRef(nameRef));
        }
        return names;
    }

    void mkdir(Path dir) throws IOException {
        var name = dir.getFileName().toString();
        var emptyEntry = -1;
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            var nameRef = getNameRef(entry);
            if (nameRef == 0 && emptyEntry == -1)
                emptyEntry = entry;
            if (name.equals(nameAtRef(nameRef)))
                throw fileAlreadyExists(dir);
        }

        if (emptyEntry == -1) {
            var enlarged = enlarge();
            enlarged.mkdir(dir);
            return;
        }

        var nameBlock = block.storage.allocateName(name);
        var dirBlock = block.storage.allocateDirectory(4, block.getRef());
        setName(emptyEntry, nameBlock);
        setObject(emptyEntry, dirBlock);
    }

    /**
     * Remove this directory from its parent directory.
     */
    void removeMe(Path dir) throws IOException {
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++)
            if (getNameRef(entry) != 0)
                throw new DirectoryNotEmptyException(dir.toString());

        var parent = getParent();
        for (int entry = 0, max = parent.getEntriesCount(); entry < max; entry++) {
            if (parent.getObjectRef(entry) == block.getRef()) {
                var nameRef = parent.getNameRef(entry);

                parent.setNameRef(entry, 0);
                parent.setObjectRef(entry, 0);

                block.ref(nameRef, BlockType.NAME).free();
                block.free();
                return;
            }
        }
    }

    /**
     * Remove the block from the directory entries, but don't free it.
     */
    void remove(Block obj) throws IOException {
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            if (getObjectRef(entry) == obj.getRef()) {
                getName(entry).free();
                setNameRef(entry, 0);
                setObjectRef(entry, 0);
                return;
            }
        }
    }

    void rename(Path oldPath, String newName) throws IOException {
        var oldEntry = -1;
        var oldName = oldPath.getFileName().toString();
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            var name = getNameString(entry);
            if (newName.equals(name))
                throw fileAlreadyExists(oldPath.resolveSibling(newName));
            if (oldName.equals(name))
                oldEntry = entry;
        }

        if (oldEntry == -1)
            throw fileNotFound(oldPath);

        var name = block.storage.allocateName(newName);
        getName(oldEntry).free();
        setName(oldEntry, name);
    }

    Directory create(Path path, String name, Block obj) throws IOException {
        var emptyEntry = -1;
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            var nameRef = getNameRef(entry);
            if (nameRef == 0 && emptyEntry == -1)
                emptyEntry = entry;
            if (name.equals(nameAtRef(nameRef)))
                throw fileAlreadyExists(path);
        }

        if (emptyEntry == -1) {
            var enlarged = enlarge();
            return enlarged.create(path, name, obj);
        }

        var nameBlock = block.storage.allocateName(name);
        setName(emptyEntry, nameBlock);
        setObject(emptyEntry, obj);
        return this;
    }

    OpenFile open(Path file, String mode) throws IOException {
        var name = file.getFileName().toString();

        var emptyEntry = -1;
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            var nameRef = getNameRef(entry);
            if (nameRef == 0 && emptyEntry == -1)
                emptyEntry = entry;
            if (name.equals(nameAtRef(nameRef)))
                return new OpenFile(new RegularFile(getObject(entry)), mode);
        }
        if (mode.equals("r"))
            throw fileNotFound(file);

        if (emptyEntry == -1) {
            var enlarged = enlarge();
            return enlarged.open(file, mode);
        }

        var nameBlock = block.storage.allocateName(name);
        var fileBlock = block.storage.allocateFile();
        setName(emptyEntry, nameBlock);
        setObject(emptyEntry, fileBlock);
        return new OpenFile(new RegularFile(fileBlock), mode);
    }

    Block lookup(String name) throws IOException {
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++)
            if (name.equals(getNameString(entry)))
                return getObject(entry);
        return null;
    }

    Directory lookupDir(String name) throws IOException {
        if (name.equals(".")) return this;
        if (name.equals("..")) return getParent();
        var entry = lookup(name);
        return entry != null ? new Directory(entry) : null;
    }

    void delete(Path file) throws IOException {
        var name = file.getFileName().toString();
        for (int entry = 0, max = getEntriesCount(); entry < max; entry++) {
            if (name.equals(getNameString(entry))) {
                var obj = getObject(entry);
                var reg = new RegularFile(obj);

                getName(entry).free();
                setNameRef(entry, 0);
                // FIXME: is setObjectRef(entry, 0) missing here?

                reg.delete();
                return;
            }
        }
        throw fileNotFound(file);
    }

    private Directory enlarge() throws IOException {
        var large = block.storage.allocateDirectory(2 * (block.getSize() / 16), getParentRef());

        var entries = new byte[block.getSize() - 8];
        block.readFully(8, entries, 0, entries.length);
        large.write(8, entries, 0, entries.length);

        var parent = getParent();
        for (int entry = 0, max = parent.getEntriesCount(); entry < max; entry++) {
            if (parent.getObjectRef(entry) == block.getRef())
                parent.setObject(entry, large);
        }

        for (int entry = 0, max = parent.getEntriesCount(); entry < max; entry++) {
            var child = getObject(entry);
            if (child.getType() == BlockType.DIRECTORY)
                new Directory(child).setParent(large);
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

    private static FileAlreadyExistsException fileAlreadyExists(Path path) {
        return new FileAlreadyExistsException(path.toString());
    }

    private static FileNotFoundException fileNotFound(Path file) {
        return new FileNotFoundException(file.toString());
    }
}
