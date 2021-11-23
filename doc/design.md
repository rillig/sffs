# Single file filesystem

This is the specification for the single-file filesystem version 1 (sffs1).

# Layers

## Storage layer

The underlying storage must provide random access to a linear 64-bit address space.

The underlying storage may have a fixed size (complete disk or partition)
or may be dynamically resizeable (regular file in another filesystem).

The storage layer provides access to individual bytes, as well as larger integer values.

Larger integer values may be saved in big endian or little endian, as determined by the magic bytes of the superblock.
The prototype implementation only handles big endian.

## Allocation layer

The allocation layer divides the linear storage into blocks whose size is a multiple of 16 bytes.

Each block has the following on-disk structure:

~~~text
  offset   type       content
       0   U32        magic number
       4   U31        size of the block data
       8   U8[size]   data
8 + size   U8[...]    zero padding to the next multiple of 16
~~~

The first two fields form the `BlockHeader`:

* The magic number specifies how to interpret the remaining bytes of the block.
* The size specifies the size of the payload data in the block, excluding the 8 bytes from the block header.

From the perspective of the allocation layer, there are 2 kinds of blocks: allocated or free.

The data of allocated blocks is not touched by the allocation layer.

A free block has the following on-disk structure:

~~~text
offset   type          content
     0   BlockHeader   magic "SFfr"
     8   BlockRef      the next free block in the chain, or 0
    16   any           undefined
~~~

After a block has been freed, its data may or may not be reset to 0.

Setting it to 0 wipes all traces of the previously stored data, which makes undeleting files impossible.

Preserving the previously stored values is faster, especially for large files. It allows to at least partially restore
the previous data. Since the magic value "SFfr" overwrites the previous magic value, the most helpful field is not
available when trying to restore accidentally deleted files.

Providing an eternal storage or unlimited undeletion is out of the scope of sffs1. For this usage,
[Git](https://git-scm.com/) or another [versioning file system](https://en.wikipedia.org/wiki/Versioning_file_system)
may be a more appropriate tool.

## Block layer

The block layer represents the filesystem as a directed graph of blocks.

A block is identified by its offset in the underlying storage, divided by 16.

### Superblock

Block 0 is the superblock, which contains basic information about the filesystem. It has the following on-disk
structure:

~~~text 
offset   type          content
     0   BlockHeader   magic "SF01"
     8   BlockRef      root directory
    16   BlockRef      first free block
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
offset   type          content
     0   BlockHeader   magic "SFde"
     8   BlockRef      parent directory
    16   BlockRef      name0
    24   BlockRef      object0
    32   BlockRef      name1
    40   BlockRef      object1
   ...
~~~

The root directory has itself as the parent.

The directories form a tree, that is, there are no loops.

The object of each directory entry may refer to a regular file or to another directory.

In a freshly allocated directory block, all names and objects point to 0.

Future directions:

* There may be a SortedDirectory block that guarantees that the entries are sorted by code point.
* There may be a HashedDirectory block that provides fast lookup, at the expense of requiring more on-disk space.

### Regular file

A regular file is a sequence of bytes. It has the following on-disk structure:

~~~text
offset   type            content
     0   BlockHeader     magic "SFre"
     8   U63             file size
    16   U31             chunk size (for large files) or 0 (for small files)
    20   zero[12]        reserved
    24   byte[...]       for small files: file content 
    24   BlockRef[...]   for large files: the chunks containing the actual file data
~~~

When a regular file is created, it starts as a "small file", consisting of a single block. The size of this block is
unspecified, and the file data is stored directly in this block.

When a regular file becomes too large for its single block, it is converted to a "large file", and its data is split
into chunks. The original block then contains a list of references to the chunks.

> Rationale: The original block is kept at its location to keep its block number the same. This avoids updating the
> directory entry and keeps the block number stable, as long as the filesystem doesn't get defragmented or otherwise
> re-organized. This block number can thus serve as an [inode](https://en.wikipedia.org/wiki/Inode).
>
> There is no such guarantee for directories. The block number of a directory may change whenever a new entry is added
> to the directory.

All chunks of a large file must have the same block size. Each chunk stores the number of bytes given in the field
"chunk size" in the main block.

Chunks for data beyond the current file size may or may not be allocated. They may be converted into free blocks at any
time.

### File chunk

When a regular file becomes too large for its single block, it is split into chunks, see [Regular file](#regular-file).

~~~text
offset   type          content
     0   BlockHeader   magic "SFch"
     8   zero[8]       padding
    16   byte[...]     chunk data
~~~

The padding ensures that the actual chunk data is aligned on a 16-bytes boundary. This allows an implementation to align
the chunk data on the natural boundary of the underlying storage medium (typically 512 or 4096 bytes).

# Possible design extensions

* sparse files
* symlinks
* sockets
* device nodes
* shared names that are not freed
* copy-on-write constant files
* redundancy (RAID)
* fault tolerance (like [in QR codes](https://en.wikipedia.org/wiki/QR_code#Error_correction))
* large file support (>= 4 GB)
* versioning
* access control (owner, groups, permissions, ACLs)
* encryption
* compression
* alternate data streams
  * precomputed file hashes
  * precompressed data, [for use in web servers](https://httpd.apache.org/docs/2.4/mod/mod_brotli.html#precompressed)

# Reference implementation

## Terminology

* `offset` is measured in bytes
* `ref` is a block reference, measured in units of 16 bytes
* `pos` is an index into a block's data section

## Quality of implementation

* each API operation must be handled atomically

## Future directions

### Tools

* defrag
* fsck
* compact
* canonicalize (for digital signatures or [reproducible builds](https://reproducible-builds.org/))
* check for steganography (unused bits, non-canonical content)
