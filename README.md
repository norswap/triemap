# Trie Map

An compressed HAMT implementation, as devised by Steindorfer & Vinju.
Their own (far more complete) implementation is [available here]
as a library named Capsule (along with links to related papers).

I implemented this to learn about the inner workings of the data structure. As such it may
be valuable for learning, but do not use for serious work (have a look at Capsule instead).

This has `put`, `get`, `size` and an iterator implemented.
Everything is immutable (transients not available).

Requires Kotlin 1.0.3+

[available here]: https://github.com/usethesource/capsule

## Ideas

An hash-array map trie (HAMT) is a trie over the hashes of its keys.
They are implemented by the `TrieMap` class.

Each `TrieMap` instance holds a reference to its root node.

Nodes come in two flavour:

- Bitmap Nodes

    These are mixed nodes: they hold both references to map entries and to sub-nodes.

    Both entries and sub-nodes are held within a single object array.

    Each bitmap node covers 5 bits of the key hash (since 2^5 = 32 = number of bits in the bitmap).
    If these 5 bits are enough to determine a unique object, then the entry is held within the
    object array. Otherwise, the entry points to a sub-node. If the bitmap node covers the last
    five bits, the sub-node is necessarily a collision node.

    The node has two bitmaps: one for entries and one for sub-nodes.

    One bit set in a bitmap indicates there is a value/sub-node for the prefix formed by the
    bitmap node prefix + the 5-bit block represented by the position of the bit.

    The array is compact: no slot is unused. The start of the array is used to store
    entries. Each entries occupies multiple 2 slots of the array, one for the key and one for the
    value. The end of the array is taken by sub-nodes.

    To determine the index of an entry/sub-node, each set bit in a bitmap is assigned an index.
    The index of a bit is defined as the number of lower-order bits in the bitmap.

    Entry indices are multiplied by 2 and indexed from the start of the array,
    while sub-node indices are index from the end of the array.

- Collision Nodes

    These nodes collect multiple values whose key have the same hash.
    Keys are distinguished via the `equals` method.
    
One of the tricky point in the algorithms is removal. When an entry is removed in a node so that
it only contains a single entry, this node will either become the new root of the trie, or
will be "inlined" in one of its ancestors. The singleton node is inlined in the first of its
ancestor that does have other entries or sub-nodes. If no such ancestor exists, it becomes the new
root node.

In this implementation, singleton nodes are always bitmap nodes: when the second-to-last node is
removed from a collision node, a bitmap node with a single item is returned.
