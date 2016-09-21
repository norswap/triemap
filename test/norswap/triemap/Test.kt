package norswap.triemap
import java.math.BigInteger
import java.util.HashMap
import java.util.Random

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    while (true)
        doSomething()
}

// -------------------------------------------------------------------------------------------------

val m1 = HashMap<String, Int>()
var m2 = TrieMap<String, Int>()

val random = Random()

// -------------------------------------------------------------------------------------------------

fun check (b: Boolean)
{
    if (!b) {
        println(m1)
        println(m2)
        println(m2.empty)
        println(m2.size)
        throw AssertionError()
    }
}

// -------------------------------------------------------------------------------------------------

fun verify()
{
    check(m1.size == m2.size)

    for (k in m1.keys) {
        check(m1[k] == m2[k])
    }
}

// -------------------------------------------------------------------------------------------------

fun doSomething()
{
    if (random.nextBoolean()) // add
    {
        val n = when (random.nextInt(3)) {
            0    -> { println("adding 1")   ; 1 }
            1    -> { println("adding 10")  ; 10 }
            else -> { println("adding 100") ; 100 }
        }

        for (i in 1..n) {
            val str = BigInteger(130, random).toString(32)
            val int = random.nextInt()
            m1.put(str, int)
            m2 = m2.put(str, int)
        }
    }
    else // remove
    {
        val n = when (random.nextInt(3)) {
            0    -> { println("remove 1")  ; 1 }
            1    -> { println("remove 9")  ; 9 }
            else -> { println("remove 90") ; 90 }
        }

        for (i in 1..n) {
            if (m1.isEmpty()) {
                check(m2.empty)
                break
            }

            val j = random.nextInt(m1.size)
            val k = m1.keys.asSequence().drop(j).iterator().next()
            m1.remove(k)
            m2 = m2.remove(k)
            check (m2.get(k) == null)
        }
    }

    println(m2.size)
    verify()
}

// -------------------------------------------------------------------------------------------------
