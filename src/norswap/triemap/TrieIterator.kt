package norswap.triemap
import java.util.ArrayDeque
import java.util.NoSuchElementException

class TrieIterator<K: Any, V: Any> (root: TrieNode<K, V>): Iterator<Pair<K, V>>
{
    // ---------------------------------------------------------------------------------------------

    private val nodes   = ArrayDeque<TrieNode<K, V>>()
    private val indices = ArrayDeque<Int>()

    private var node    = root
    private var i       = 0

    // ---------------------------------------------------------------------------------------------

    override fun hasNext()
        = !nodes.isEmpty()

    // ---------------------------------------------------------------------------------------------

    init { advance() }

    // ---------------------------------------------------------------------------------------------

    /**
     * `(node, i) = pop(nodes, indices)`
     */
    private fun pop(): Boolean
    {
        if (nodes.isEmpty())
            return false

        node = nodes.pop()
        i = indices.pop()
        return true
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * ````
     * top(nodes, indices) = (node, i + 1)
     * (node, i) = (node1, 0)
     * ````
     */
    private fun push (node1: TrieNode<K, V>)
    {
        nodes.push(node)
        indices.push(i + 1)
        node = node1
        i = 0
    }

    // ---------------------------------------------------------------------------------------------

    fun advance()
    {
        outer@ while (true)
        {
            val n = node
            if (n is CollisionNode)
            {
                if (i < n.size)
                    return
                if (!pop())
                    return
                else
                    continue
            }
            else if (n is BitmapNode)
            {
                inner@ while (true)
                {
                    if (i == PARTITION_RANGE)
                        if (!pop())
                            return
                        else
                            continue@outer

                    val bit = bit(i)

                    if (n.isEntry(bit)) {
                        return
                    }
                    else if (n.isNode(bit)) {
                        push(n.node(bit))
                        continue@outer
                    }
                    else {
                        ++ i
                        continue@inner
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun <T: Any> nextGen(
        collision: CollisionNode<K, V>.() -> T,
        bitmap: BitmapNode<K, V>.() -> T)
        : T
    {
        if (nodes.isEmpty())
            throw NoSuchElementException()

        val n = node

        val out = when (n)
        {
            is CollisionNode    -> n.collision()
            is BitmapNode       -> n.bitmap()
            else                -> throw Error()
        }

        ++ i
        advance()
        return out
    }

    // ---------------------------------------------------------------------------------------------

    override fun next(): Pair<K, V>
        = nextGen (
            collision = { Pair(key(i), value(i)) },
            bitmap    = { val j = entryIndex(bit(i)) ; Pair(key(j), value(j)) }
        )

    // ---------------------------------------------------------------------------------------------

    fun nextKey(): K
        = nextGen (
            collision = { key(i) },
            bitmap    = { key(bit(i)) }
        )

    // ---------------------------------------------------------------------------------------------

    fun nextValue(): V
        = nextGen (
            collision = { value(i) },
            bitmap    = { value(bit(i)) }
        )

    // ---------------------------------------------------------------------------------------------
}
