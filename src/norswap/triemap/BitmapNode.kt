@file:Suppress("NOTHING_TO_INLINE")
package norswap.triemap
import norswap.triemap.TrieNode.SizePredicate.*

class BitmapNode<K: Any, V: Any> (nodeMap: Int, entryMap: Int, items: Array<Any>): TrieNode<K, V>()
{
    // ---------------------------------------------------------------------------------------------

    private val nodeMap  = nodeMap
    private val entryMap = entryMap
    private val items    = items

    // ---------------------------------------------------------------------------------------------

    internal inline fun isEntry (bit: Int)
        = (entryMap and bit) != 0

    internal inline fun isNode (bit: Int)
        = (nodeMap and bit) != 0

    // ---------------------------------------------------------------------------------------------

    private inline fun entryCount()
        = Integer.bitCount(entryMap)

    private inline fun nodeCount()
        = Integer.bitCount(nodeMap)

    // ---------------------------------------------------------------------------------------------

    internal inline fun entryIndex (bit: Int)
        = index(entryMap, bit)

    internal inline fun key (i: Int): K
        = items[2 * i].cast()

    internal inline fun value (i: Int): V
        = items[2 * i + 1].cast()

    internal inline fun node (bit: Int): TrieNode<K, V>
        = items[itemNodeIndex(bit)].cast()

    // ---------------------------------------------------------------------------------------------

    internal inline fun itemEntryIndex (bit: Int)
        = 2 * entryIndex(bit)

    private inline fun itemNodeIndex (bit: Int, size: Int = items.size)
        = size - 1 - index(nodeMap, bit)

    // ---------------------------------------------------------------------------------------------

    override val sizePred: TrieNode.SizePredicate =
        if (nodeCount() != 0)
            MANY
        else when (entryCount()) {
            0    -> ZERO
            1    -> ONE
            else -> MANY
        }

    // ---------------------------------------------------------------------------------------------

    override fun find (k: K, h: Int, shift: Int): V?
    {
        val bit = bitFor(h, shift)

        if (isEntry(bit)) {
            val i = entryIndex(bit)
            return if (key(i) == k) value(i) else null
        }

        if (isNode(bit))
            return node(bit).find(k, h, shift + PARTITION_SIZE)

        return null
    }

    // ---------------------------------------------------------------------------------------------

    override fun updated (k: K, v: V, h: Int, shift: Int, change: Change<K, V>): TrieNode<K, V>
    {
        val bit = bitFor(h, shift)

        if (isEntry(bit))
        {
            val i = entryIndex(bit)
            val k1 = key(i)
            val v1 = value(i)

            if (k == k1)
            {
                change.replaced(v1)
                return updateValue(bit, v1)
            }
            else
            {
                val h1 = k1.hashCode()
                val subNode = merge(k, v, h, k1, v1, h1, shift + PARTITION_SIZE)
                change.changed()
                return entryToNode(bit, subNode)
            }
        }
        else if (isNode(bit))
        {
            val subNode = node(bit).updated(k, v, h, shift + PARTITION_SIZE, change)
            return if (change.changed) updateNode(bit, subNode) else this
        }
        else
        {
            change.changed()
            return insertEntry(bit, k, v)
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun updateValue (bit: Int, v: V): TrieNode<K, V>
    {
        val i = itemEntryIndex(bit) + 1
        val items1 = items.clone()
        items1[i] = v
        return BitmapNode(nodeMap, entryMap, items1)
    }

    // ---------------------------------------------------------------------------------------------

    private fun entryToNode (bit: Int, node: TrieNode<K, V>): TrieNode<K, V>
    {
        val size1 =  items.size - 1
        val items1 = array(size1)

        val i0 = itemEntryIndex(bit)
        val i1 = itemNodeIndex(bit, size1)

        assert(i0 <= i1)
        System.arraycopy(items, 0,      items1, 0,      i0)
        System.arraycopy(items, i0 + 2, items1, i0,     i1 - i0)
        System.arraycopy(items, i1 + 2, items1, i1 + 1, items.size - i1 - 2)
        items1[i1] = node

        return BitmapNode(nodeMap or bit, entryMap xor bit, items1)
    }

    // ---------------------------------------------------------------------------------------------

    private fun updateNode (bit: Int, node: TrieNode<K, V>): TrieNode<K, V>
    {
        val i = itemNodeIndex(bit)
        val nodes1 = items.clone()
        nodes1[i] = node
        return BitmapNode(nodeMap, entryMap, nodes1)
    }

    // ---------------------------------------------------------------------------------------------

    private fun insertEntry (bit: Int, k: K, v: V): TrieNode<K, V>
    {
        val i = itemEntryIndex(bit)
        val nodes1 = array(items.size + 2)

        System.arraycopy(items, 0, nodes1, 0,       i)
        System.arraycopy(items, i, nodes1, i + 2,   items.size - i)
        nodes1[i]     = k
        nodes1[i + 1] = v

        return BitmapNode(nodeMap, entryMap or bit, nodes1)
    }

    // ---------------------------------------------------------------------------------------------

    private fun merge (k1: K, v1: V, h1: Int, k2: K, v2: V, h2: Int, shift: Int): TrieNode<K, V>
    {
        if (shift > HASH_LEN) {
            assert(h1 == h2)
            return CollisionNode(array(k1, k2), array(v1, v2), h1)
        }

        val p1 = part(h1, shift)
        val p2 = part(h2, shift)

        if (p1 != p2)
        {
            val entryMap1 = bit(p1) or bit(p2)

            if (p1 < p2)
                return BitmapNode(0, entryMap1, arrayOf(k1, v1, k2, v2))
            else
                return BitmapNode(0, entryMap1, arrayOf(k2, v2, k1, v1))
        }
        else
        {
            val subNode = merge(k1, v1, h1, k2, v2, h2, shift + PARTITION_SIZE)
            return BitmapNode<K, V>(bit(p1), 0, arrayOf(subNode))
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun removed (k: K, h: Int, shift: Int, change: Change<K, V>): TrieNode<K, V>
    {
        val bit = bitFor(h, shift)

        if (isEntry(bit))
        {
            val i = entryIndex(bit)

            if (key(i) != k)
                return this

            val v = value(i)
            change.replaced(v)

            if (entryCount() != 2 || nodeCount() != 0)
                return removeEntry(bit)

            // Compute the bit with shift = 0.
            // Either the node will become the root (shift = 0),
            // or the node will be inlined (then only the bit count is used).

            val entryMap1 =
                if (shift == 0) // remove the bit for delete node
                    entryMap xor bit
                else // entries will have the same bit at shift=0
                    bitFor(h, 0)

            if (i == 0)
                return BitmapNode(0, entryMap1, arrayOf(key(1), value(1)))
            else
                return BitmapNode(0, entryMap1, arrayOf(key(0), value(0)))
        }
        else if (isNode(bit))
        {
            val subNode = node(bit).removed(k, h, shift + PARTITION_SIZE, change)

            if (!change.changed)
                return this

            when (subNode.sizePred) {
                ONE  ->
                    if (entryCount() == 0 && nodeCount() == 1)
                        // Single sub-node with single entry, will become root or be inlined.
                        return subNode
                    else
                        // Inline sub-node in this node.
                        return nodeToEntry(bit, subNode)
                MANY ->
                    return updateNode(bit, subNode)
                ZERO ->
                    throw IllegalStateException("Sub-node must have at least one element.")
            }
        }

        return this
    }

    // ---------------------------------------------------------------------------------------------

    private fun removeEntry (bit: Int): TrieNode<K, V>
    {
        val i = itemEntryIndex(bit)
        val items1 = array(items.size - 2)

        System.arraycopy(items, 0,      items1, 0, i)
        System.arraycopy(items, i + 2,  items1, i, items.size - i - 2)

        return BitmapNode(nodeMap, entryMap xor bit, items1)
    }

    // ---------------------------------------------------------------------------------------------

    private fun nodeToEntry (bit: Int, node: TrieNode<K, V>): TrieNode<K, V>
    {
        val items1 = array(items.size + 1)

        val i0 = itemEntryIndex(bit)
        val i1 = itemNodeIndex(bit)

        assert(i0 <= i1)
        System.arraycopy(items, 0,      items1, 0,      i0)
        System.arraycopy(items, i0,     items1, i0 + 2, i1 - i0)
        System.arraycopy(items, i1 + 1, items1, i1 + 2, items.size - i1 - 1)

        assertT (node as BitmapNode<K, V>)

        items1[i0]      = node.key(0)
        items1[i0 + 1]  = node.value(0)

        return BitmapNode(nodeMap xor bit, entryMap or bit, items1)
    }

    // ---------------------------------------------------------------------------------------------

    override fun hashCode(): Int
    {
        var h = 0
        h = h * 31 + nodeMap
        h = h * 31 + entryMap
        h = h * 31 + items.hash()
        return h
    }

    // ---------------------------------------------------------------------------------------------

    override fun equals (other: Any?)
        =  other === this
        || other is BitmapNode<*, *>
        && nodeMap == other.nodeMap
        && entryMap == other.entryMap
        && items eq other.items

    // ---------------------------------------------------------------------------------------------
}
