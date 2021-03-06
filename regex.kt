interface Expr {
    fun match(target: String, i: Int, cont: (rest: String, pos: Int) -> Boolean): Boolean
}


/**
 * implements ab
 */
class Concat constructor(val left: Expr, val right: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return left.match(target, i, { rest, pos ->
            right.match(rest, pos, cont)
        })
    }
}

/**
 * implements |
 */
class Alternative constructor(val left: Expr, val right: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return left.match(target, i, cont) || right.match(target, i, cont)
    }
}


/**
 * implements *
 */
class Repeat constructor(val pattern: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return pattern.match(target, i, { rest, pos ->
            match(rest, pos, cont) || cont(rest, pos)
        }) || cont(target, i)
    }
}


/**
 * implements ?
 */
class Optional constructor(val pattern: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return pattern.match(target, i, cont) || cont(target, i)
    }
}


/**
 * implements *|+|?|{a,b}|{a,}|{,b}  greedy not greedy
 */
class Quantity constructor(val pattern: Expr, var min: Int = 0, var max: Int = Integer.MAX_VALUE, val geedy: Boolean = true) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return when (true) {
            min > 0 ->
                pattern.match(target, i, { rest, pos ->
                    consume()
                    match(rest, pos, cont)
                })

            max <= 0 ->
                cont(target, i)

            else ->
                pattern.match(target, i, { rest, pos ->
                    consume()
                    match(rest, pos, cont)
                }) || (fallback() && cont(target, i))
        }
    }


    // FIXME
    // should not retain max & min in matcher, it will fails the rollback
    fun consume() {
        if (max != Int.MAX_VALUE) max--
        min--
    }

    fun fallback() : Boolean {
        if(max != Int.MAX_VALUE) max++
        min++
        return true
    }
}


/**
 * implements .
 */
class Any constructor() : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return target.length() > i && cont(target, i + 1)
    }
}


/**
 * implements \w
 */
class Word constructor() : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return target.length() > i && target[i] in ('a'..'z') + ('A'..'Z') + ('0'..'9') + '_' && cont(target, i + 1)
    }
}


/**
 * implements \d
 */
class Digital constructor() : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return target.length() > i && target[i] in '0'..'9' && cont(target, i + 1)
    }
}

/**
 * implements \s
 */
class Blank constructor() : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return target.length() > i && target[i] in "\n\r\t\u200b\u200c" && cont(target, i + 1)
    }
}


/**
 * implements accurate match
 */
class Ch constructor(val ch: Char) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return target.length() > i && target[i] == ch && cont(target, i + 1)
    }
}


fun match(pattern: Expr, subject: String) {
    val res = pattern.match(subject, 0, {
        subject, pos ->
        subject.length() == pos
    })
    println(subject + ":" + res)
}

fun main(args: Array<String>) {
    // mn*p?a|mi
    /*
    val pattern = Alternative(
            Concat(
                    Ch('m'),
                    Concat(
                            Repeat(Ch('n')),
                            Concat(Optional(Ch('p')),
                                    Ch('a')))),
            Concat(Ch('m'), Ch('i'))
    )

    match(pattern, "mi")
    match(pattern, "mnnnnnnpa")
    match(pattern, "ma")
    match(pattern, "m")
    match(pattern, "konata");
    */

    // (mn{2,3}a*b?\w\d+|\d?)e
    // (
    // mn{2,3}a*b?\w\d+
    // |
    // \d?
    // )
    //
    //
    // e

    /*
    val pattern2 = Concat(
            Concat(Ch('e'),
                    Concat(Quantity( Ch('n'),2,3 ),
                        Concat(Quantity(Ch('a'),0,Int.MAX_VALUE),
                            Concat(Quantity(Ch('b'),0,1),
                                Concat(Word(),Digital()
                                )
                            )
                        )
                    )
            )
            ,
            Ch('e')
    )
    */


    // n*|m*
    val pattern3 = Alternative(
            Quantity(Ch('n'), 0, Int.MAX_VALUE),
            Quantity(Ch('m'), 1, Int.MAX_VALUE)
    )

    // (mn{2,3}a*b?\w\d+|\d?\w+?))ni

    match(pattern3, "n")
    match(pattern3, "nnnn")
    match(pattern3, "m")
    match(pattern3, "mmmm")
    match(pattern3, "mnmnmn")
    match(pattern3, "fdsafdsa")



    // \w{2,5}\d{,7}|\s{5}\d{3,}

    val pattern4 = Alternative(
    Concat(
    Quantity(Word(), 2, 5),
    Quantity(Digital(), 0, 7)
    ),

    Concat(
            Quantity(Blank(), 5, 5),
            Quantity(Digital(), 3, Int.MAX_VALUE)
    ))

    println("====================")
    match(pattern4,"ab123") // true
    match(pattern4,"  1234") // false
    match(pattern4,"  123") // false
    match(pattern4,"     1245") // true
    match(pattern4,"   ") // false
    match(pattern4,"fdafdd1") // false
    match(pattern4,"ffff") // true

    val quantityPattern = Quantity(Digital(),2,5)
    match(quantityPattern,"")
    match(quantityPattern,"12")
    match(quantityPattern,"1234")
    match(quantityPattern,"123443434")




}




