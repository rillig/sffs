# Single file filesystem

This is the specification for the single-file filesystem version 1 (sffs1).

# Layers

## Storage layer

The underlying storage must provide random access to a linear 64-bit address space.

The underlying storage may have a fixed size (complete disk or partition)
or may be dynamically resizeable (regular file in another filesystem).

The storage layer provides access to individual bytes, as well as larger integer values.

Larger integer values may be saved in big endian or little endian, as determined by the magic bytes of the superblock.
The prototyp implementation only handles big endian.

## Allocation layer

The allocation layer divides the linear storage into blocks of size 16.

Each block has the following on-disk structure:

~~~text
offset      type       content
0000_0000   U32        type
0000_0004   U32        size of the block data
0000_0008   U8[size]   data
8 + size    U8[...]    zero padding to the next multiple of 16
~~~

The first two fields form the `BlockHeader`.

From the perspective of the allocation layer, there are 2 kinds of blocks: allocated or free.

The data of allocated blocks is not touched by the allocation layer.

A free block has the following on-disk structure:

~~~text
offset      type   content
0000_0000   U32    magic "SFfr"
0000_0004   U32    size of the block data
0000_0008   U64    block index of the next free block in the chain
0000_0010   any    undefined
~~~

After a block has been freed, its data may or may not be reset to 0.

Setting it to 0 wipes all traces of the previously stored data, which makes undeleting files impossible.

Preserving the previously stored values is faster, especially for large files. It allows to at least partially restore
the previous data. Since the magic value "SFfr" overwrites the previous magic value, the most helpful field is not
available when trying to restore accidentally deleted files.

Providing an eternal storage or unlimited undeletion is out of the scope of this filesystem. For this usage,
[Git](https://git-scm.com/) may be a more appropriate tool.

## Block layer

The block layer represents the filesystem as a directed graph of blocks.

A block is identified by its offset in the underlying storage, divided by 16.

### Superblock

Block 0 is the superblock, which contains basic information about the filesystem. It has the following on-disk
structure:

~~~text 
offset
0000_0000   BlockHeader   magic "SF01"
0000_0008   BlockRef      root directory
0000_0010   BlockRef      first free block
~~~

Since no other block needs to refer to the superblock, the block number 0 means an absent block, for example in lists of
directory entries.

### Name

A directory in a filesystem is a mapping from names to filesystem objects. A name is a sequence of Unicode code points,
encoded as UTF-8.

Limitations:

* The code points U+0000 "Null", U+002F "Solidus", U+005C "Reverse solidus" must not occur in a name.
* The name must neither be `.` (dot) nor `..` ([dot-dot](https://9p.io/sys/doc/lexnames.html)).

The filesystem is case-sensitive.

The filesystem does not perform [Unicode normalization](https://unicode.org/reports/tr15/).

The filesystem does not prevent the use of
[visually indistinguishable code point sequences](https://en.wikipedia.org/wiki/Homoglyph).

### Directory

A directory maps names to filesystem objects. It has the following on-disk structure:

~~~text
offset
0000_0000   BlockHeader   magic "SFde"
0000_0008   BlockRef      parent directory
0000_0010   BlockRef      name0
0000_0018   BlockRef      object0
0000_0020   BlockRef      name1
0000_0020   BlockRef      object1
...
~~~

The object of the directory entry may refer to a regular file or to another directory.

In a freshly allocated directory block, all names and objects point to 0.

Future directions:

* There may be a SortedDirectory block that guarantees that the entries are sorted by code point.
* There may be a HashedDirectory block that provides fast lookup, at the expense of requiring more on-disk space.

### Regular file

A regular file is a sequence of bytes. It has the following on-disk structure:

~~~text
offset
0000_0000   BlockHeader   magic "SFre"
0000_0008   byte[size]    file content
~~~

Out of scope:

* sffs1 does not support file permissions.
* sffs1 does not support file attributes.
* sffs1 does not support alternate data streams.

# Possible design extensions

* symlinks
* sockets
* device nodes
* large file support (>= 4 GB)
* access control (owner, groups, permissions, ACLs)
* encryption
* compression
* alternate data streams
    * precomputed file hashes
    * precompressed data, [for use in web servers](https://httpd.apache.org/docs/2.4/mod/mod_brotli.html#precompressed)

# Reference implementation

## Future directions

### Tools

* defrag
* fsck
* compact
* canonicalize (for digital signatures or [reproducible builds](https://reproducible-builds.org/))
* check for steganography (unused bits, non-canonical content)
