@file:Suppress("NOTHING_TO_INLINE", "CAST_NEVER_SUCCEEDS")
package norswap.triemap

abstract class TrieNode<K: Any, V: Any>
{
    // ---------------------------------------------------------------------------------------------

    companion object
    {
        private val EMPTY_NODE
            = BitmapNode<Nothing, Nothing>(0, 0, emptyArray())

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        inline internal fun <K: Any, V: Any> emptyNode()
            = EMPTY_NODE as TrieNode<K, V>
    }

    // ---------------------------------------------------------------------------------------------

    abstract val sizePred: SizePredicate

    // ---------------------------------------------------------------------------------------------

    enum class SizePredicate { ZERO, ONE, MANY }

    // ---------------------------------------------------------------------------------------------

    abstract fun find (k: K, h: Int, shift: Int): V?

    // ---------------------------------------------------------------------------------------------

    abstract fun updated (k: K, v: V, h: Int, shift: Int, change: Change<K, V>): TrieNode<K, V>

    // ---------------------------------------------------------------------------------------------

    abstract fun removed (k: K, h: Int, shift: Int, change: Change<K, V>): TrieNode<K, V>

    // ---------------------------------------------------------------------------------------------
}
