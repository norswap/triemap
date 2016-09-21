@file:Suppress("NOTHING_TO_INLINE")
package norswap.triemap
import java.util.Arrays

// -------------------------------------------------------------------------------------------------

/**
 * Like [indexOf], except inlined & more efficient.
 */
inline fun <T> Array<T>.index(it: T): Int
{
    var i = 0
    while (i < this.size) {
        if (this[i] == it) return i
        ++ i
    }
    return -1
}

// -------------------------------------------------------------------------------------------------

@Suppress("SENSELESS_COMPARISON")
/**
 * Usage `assertT(thing as MyType)`.
 *
 * When a cast appears in an expression, Kotlin enables smart-casting for the remainder of the
 * scope. This simply enables this form of casting with no run-time overhead when assertions
 * are turned off.
 */
inline fun assertT(any: Any) {
    assert(any != null)
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the result of [f].
 * The point is to allow statements in an expression context.
 */
inline fun <T> expr(f: () -> T): T = f()

// -------------------------------------------------------------------------------------------------

/**
 * Shorthand for [StringBuilder.append].
 */
operator inline fun StringBuilder.plusAssign(o: Any?) { append(o) }

// -------------------------------------------------------------------------------------------------

/**
 * Shorthand for [Arrays.hashCode].
 */
inline fun Array<*>.hash()
    = Arrays.hashCode(this)

// -------------------------------------------------------------------------------------------------

/**
* Shorthand for [Arrays.equals].
*/
infix inline fun Array<*>.eq(other: Array<*>)
    = Arrays.equals(this, other)

// -------------------------------------------------------------------------------------------------

/**
 * To create an array with items whose type is non-reifiable ([arrayOf] chokes on these cases).
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> array(vararg items: T)
    =  items as Array<T>

// -------------------------------------------------------------------------------------------------

/**
 * Allocate a null-initialized array (circumvents Kotlin nullability checks).
 */
@Suppress("CAST_NEVER_SUCCEEDS")
inline fun array(size: Int): Array<Any>
    = arrayOfNulls<Any>(size) as Array<Any>

// -------------------------------------------------------------------------------------------------

/**
 * Allocates an null-initialized array of type T (circumvents Kotlin nullability checks).
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> arrayT(size: Int)
    = arrayOfNulls<Any>(size) as Array<T>

// -------------------------------------------------------------------------------------------------

/**
 * Just like [Arrays.copyOf], but does not mess withclasses, hence faster (on the other hand,
 * less safe: using an Object array delays or inhibits class casts exceptions).
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> copyOf(array: Array<T>, size: Int): Array<T>
{
    val array1 = arrayOfNulls<Any>(size) as Array<T>
    val copy = if (size < array.size) size else array.size
    System.arraycopy(array, 0, array1, 0, copy)
    return array1
}

// -------------------------------------------------------------------------------------------------

/**
 * Casts the receiver to [T].
 *
 * This is more useful than regular casts because it enables casts to non-denotable types
 * through type inference.
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> Any?.cast()
    = this as T

// -------------------------------------------------------------------------------------------------
