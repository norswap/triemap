@file:Suppress("UNCHECKED_CAST")
package norswap.triemap
import kotlin.text.removeSuffix

/**
 * An immutable map backed by a hash array mapped trie (HAMT).
 */
class TrieMap<K: Any, V: Any> private constructor (root: TrieNode<K, V>, h: Int, size: Int)
: ImmutableMap<K, V>, Iterable<Pair<K, V>>
{
    // ---------------------------------------------------------------------------------------------

    companion object
    {
        private val EMPTY_MAP = TrieMap<Nothing, Nothing>(TrieNode.emptyNode(), 0, 0)

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        /**
         * Returns an empty trie map. This does not trigger any memory allocation.
         */
        operator fun <K: Any, V: Any> invoke(): TrieMap<K, V>
            = EMPTY_MAP as TrieMap<K, V>

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        /**
         * Returns a trie map whose keys and values are passed in [keyValuePairs].
         * e.g. `TrieMap(k0, v0, k1, v1, k2, v2)`
         */
        operator fun <K: Any, V: Any> invoke (vararg keyValuePairs: Any): TrieMap<K, V>
        {
            if (keyValuePairs.size % 2 != 0)
                throw IllegalArgumentException(
                    "Length of argument list is uneven: no key/value pairs.")

            var result = TrieMap<K, V>()

            var i = 0
            while (i < keyValuePairs.size)
            {
                val k = keyValuePairs[i] as K
                val v = keyValuePairs[i + 1] as V
                result = result.put(k, v)
                i += 2
            }

            return result
        }
    }

    // ---------------------------------------------------------------------------------------------

    private  val  root  = root
    private  val  h     = h
    override val size   = size

    // ---------------------------------------------------------------------------------------------

    override fun get (k: K): V?
        = root.find(k, k.hashCode(), 0)

    // ---------------------------------------------------------------------------------------------

    override fun put (k: K, v: V): TrieMap<K, V>
    {
        val kh = k.hashCode()
        val change = Change<K, V>()
        val root1 = root.updated(k, v, kh, 0, change)

        if (!change.changed)
            return this

        val rep = change.replaced
        val vh1 = v.hashCode()

        if (rep != null) {
            val vh0 = rep.hashCode()
            val h1 = h + (kh xor vh1) - (kh xor vh0)
            return TrieMap(root1, h1, size)
        }
        else {
            val h1 = h + (kh xor vh1)
            return TrieMap(root1, h1, size + 1)
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun remove (k: K): TrieMap<K, V>
    {
        val kh = k.hashCode()
        val change = Change<K, V>()
        val root1 = root.removed(k, kh, 0, change)

        val rep = change.replaced

        if (rep != null) {
            val vh = rep.hashCode()
            return TrieMap(root1, h - (kh xor vh), size - 1)
        }

        return this
    }

    // ---------------------------------------------------------------------------------------------

    override fun hashCode() = h

    // ---------------------------------------------------------------------------------------------

    override fun equals (other: Any?)
        =  other === this
        || other is TrieMap<*, *>
        && other.size == size
        && other.h == h
        && other.root == root
        || other is Map<*, *>
        && other.size == size
        && expr e@ {
            for (e in other.entries) {
                val k = e.key as K
                val v = get(k)
                if (v != e.value)
                    return@e false
            }
            true
        }

    // ---------------------------------------------------------------------------------------------

    override fun iterator() = TrieIterator(root)

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
    {
        val b = StringBuilder()
        b += "{"
        for ((k, v) in this) b += " $k -> $v,"
        if (!empty) {
            b.removeSuffix(",")
            b += " "
        }
        b += "}"
        return b.toString()
    }

    // ---------------------------------------------------------------------------------------------
}
