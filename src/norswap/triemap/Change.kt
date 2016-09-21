package norswap.triemap

/**
 * Used to report that the tree was changed:
 * - entry inserted
 * - entry removed
 * - value for key replaced
 *
 * In the last case, [replaced] holds the old value.
 */
class Change<K: Any, V: Any>
{
    // ---------------------------------------------------------------------------------------------

    var replaced: V? = null
    var changed = false

    // ---------------------------------------------------------------------------------------------

    /**
     * Call if the element count changed.
     */
    fun changed()
    {
        changed = true
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call with the old value if it was replaced by a new one.
     */
    fun replaced (v: V)
    {
        changed = true
        this.replaced = v
    }

    // ---------------------------------------------------------------------------------------------
}
