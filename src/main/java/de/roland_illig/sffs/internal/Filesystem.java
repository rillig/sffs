package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;

final class Filesystem implements de.roland_illig.sffs.Filesystem {

    private final Storage storage;

    /**
     * @see RandomAccessFile#RandomAccessFile(File, String)
     */
    Filesystem(File f, String mode) throws IOException {
        this.storage = new Storage(new RandomAccessFile(f, mode));
    }

    @Override
    public void mkdir(Path dir) throws IOException {
        var parent = lookup(dir, -1);
        if (parent == null) throw fileNotFound(dir.getParent());
        parent.mkdir(dir);
    }

    @Override
    public void rmdir(Path dir) throws IOException {
        var d = lookup(dir, 0);
        if (d == null) throw fileNotFound(dir);
        d.removeMe(dir);
    }

    @Override
    public List<String> readdir(Path dir) throws IOException {
        var d = lookup(dir, 0);
        if (d == null) throw fileNotFound(dir);
        return d.readdir();
    }

    @Override
    public void rename(Path path, String newName) throws IOException {
        var dir = lookup(path, -1);
        if (dir == null) throw fileNotFound(path);
        dir.rename(path, newName);
    }

    @Override
    public void move(Path oldPath, Path newPath) throws IOException {
        var oldDir = lookup(oldPath, -1);
        if (oldDir == null)
            throw fileNotFound(oldPath.getParent());

        var old = oldDir.lookup(oldPath.getFileName().toString());
        if (old == null)
            throw fileNotFound(oldPath);

        var newParent = lookup(newPath, -1);
        if (newParent == null)
            throw fileNotFound(newPath);
        var newName = newPath.getFileName().toString();
        var newEntry = newParent.lookup(newName);
        if (newEntry != null && newEntry.getType() == BlockType.DIRECTORY) {
            newParent = new Directory(newEntry);
            newName = oldPath.getFileName().toString();
        }

        checkAncestry(oldPath, old, newPath, newParent);

        var newParentRef = newParent.block.getRef();
        newParent = newParent.create(newPath, newName, old);
        if (oldDir.block.getRef() == newParentRef)
            oldDir = newParent;
        oldDir.remove(old);
        if (old.getType() == BlockType.DIRECTORY)
            new Directory(old).setParent(newParent.block);
    }

    private void checkAncestry(Path oldPath, Block old, Path newPath, Directory newParent) throws IOException {
        if (old.getType() != BlockType.DIRECTORY)
            return;

        var dir = newParent;
        while (true) {
            var parent = dir.getParent();
            if (parent.block.getRef() == old.getRef())
                throw new IOException("cannot move '" + oldPath + "' to its own child directory '" + newPath + "'");
            if (parent.block.getRef() == dir.block.getRef())
                return;
            dir = parent;
        }
    }

    @Override
    public void delete(Path file) throws IOException {
        var parent = lookup(file, -1);
        if (parent == null) throw fileNotFound(file.getParent());
        parent.delete(file);
    }

    @Override
    public OpenFile open(Path file, String mode) throws IOException {
        var dir = lookup(file, -1);
        if (dir == null) throw fileNotFound(file);
        return dir.open(file, mode);
    }

    @Override
    public void close() throws IOException {
        storage.close();
    }

    private Directory lookup(Path path, int rtrim) throws IOException {
        var superblock = new Superblock(storage);
        var cwd = superblock.getRootDirectory();
        for (int i = 0, n = path.getNameCount() + rtrim; cwd != null && i < n; i++)
            cwd = cwd.lookupDir(path.getName(i).toString());
        return cwd;
    }

    static FileNotFoundException fileNotFound(Path path) {
        return new FileNotFoundException(path.toString());
    }
}
