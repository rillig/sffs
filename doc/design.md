# Single file filesystem

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

From the perspective of the allocation layer, there are 2 kinds of blocks: allocated or free.

The contents of allocated blocks is not touched by the allocation layer.

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
the previous data.

## Block layer

The block layer represents the filesystem as a directed graph of blocks.

A block is identified by its offset in the underlying storage, divided by 16.

Block 0 is the superblock, which contains basic information about the filesystem. It has the following on-disk
structure:

~~~text 
offset
0000_0000   U32     magic "SFsu"
0000_0004   U32     size of the block data
0000_0008   Block   root directory
0000_0010   Block   first free block
~~~

Since no other block needs to refer to the superblock, the block number 0 means an absent block, for example in lists of
directory entries.
