@file:Suppress("NOTHING_TO_INLINE")
package norswap.triemap

class CollisionNode<K: Any, V: Any> (ks: Array<K>, vs: Array<V>, h: Int): TrieNode<K, V>()
{
    // ---------------------------------------------------------------------------------------------

    private val ks = ks
    private val vs = vs
    private val h  = h

    // ---------------------------------------------------------------------------------------------

    internal inline fun key (i: Int): K
        = ks[i]

    internal inline fun value (i: Int): V
        = vs[i]

    // ---------------------------------------------------------------------------------------------

    override val sizePred = SizePredicate.MANY

    // ---------------------------------------------------------------------------------------------

    val size: Int
        get() = ks.size

    // ---------------------------------------------------------------------------------------------

    override fun find (k: K, h: Int, shift: Int): V?
    {
        assert(this.h == h)
        val i = ks.index(k)
        return if (i >= 0) vs[i] else null
    }

    // ---------------------------------------------------------------------------------------------

    override fun updated (k: K, v: V, h: Int, shift: Int, change: Change<K, V>): TrieNode<K, V>
    {
        assert(this.h == h)
        val i = ks.index(k)

        if (i >= 0)
        {
            val v1 = vs[i]

            if (v1 == v)
                return this

            val vs1 = vs.clone()
            vs1[i] = v1

            change.replaced(v1)
            return CollisionNode(ks, vs1, h)
        }
        else
        {
            val ks1 = copyOf(ks, ks.size + 1)
            ks1[ks.size] = k

            val vs1 = copyOf(vs, vs.size + 1)
            vs1[vs.size] = v

            change.changed()
            return CollisionNode(ks1, vs1, h)
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun removed (k: K, h: Int, shift: Int, change: Change<K, V>): TrieNode<K, V>
    {
        assert(this.h == h)
        val i = ks.index(k)

        if (i < 0)
            return this

        val v0 = vs[i]
        change.replaced(v0)

        if (ks.size == 1)
        {
            return emptyNode()
        }
        else if (ks.size == 2)
        {
            val k1 = if (i == 0) ks[1] else ks[0]
            val v1 = if (i == 0) vs[1] else vs[0]

            // Compute the bit with shift = 0.
            // Either the node will become the root (shift = 0),
            // or the node will be inlined (then only the bit count is used).

            return emptyNode<K, V>().updated(k1, v1, k1.hashCode(), 0, change)
        }
        else
        {
            val ks1 = arrayT<K>(ks.size - 1)
            System.arraycopy(ks, 0,     ks1, 0, i)
            System.arraycopy(ks, i + 1, ks1, i, ks.size - i - 1)

            val vs1 = arrayT<V>(vs.size - 1)
            System.arraycopy(vs, 0,     vs1, 0, i)
            System.arraycopy(vs, i + 1, vs1, i, vs.size - i - 1)

            return CollisionNode(ks1, vs1, h)
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun hashCode(): Int
    {
        var h = 0
        h = h * 31 + this.h
        h = h * 31 + ks.hash()
        h = h * 31 + vs.hash()
        return h
    }

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    override fun equals (other: Any?)
        =  other === this
        || other is CollisionNode<*, *>
        && h == other.h
        && ks.size == other.ks.size
        && expr e@ {
            for (i in ks.indices) {
                val k0 = ks[i]
                val v0 = vs[i]
                val v1 = (other as CollisionNode<K, V>).find(k0, k0.hashCode(), 0)
                if (v0 != v1)
                    return@e false
            }
            true
        }

    // ---------------------------------------------------------------------------------------------
}
