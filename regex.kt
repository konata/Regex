interface Expr {
    fun match(target: String, i: Int, cont: (rest: String, pos: Int) -> Boolean): Boolean
}

class Concat constructor(val left: Expr, val right: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return left.match(target, i, { rest, pos ->
            right.match(rest, pos, cont)
        })
    }
}

class Alternative constructor(val left: Expr, val right: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return left.match(target, i, cont) || right.match(target, i, cont)
    }
}

class Repeat constructor(val pattern: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return pattern.match(target, i, { rest, pos ->
            match(rest, pos, cont) || cont(rest, pos)
        }) || cont(target, i)
    }
}

class Optional constructor(val pattern: Expr) : Expr {
    override fun match(target: String, i: Int, cont: (String, Int) -> Boolean): Boolean {
        return pattern.match(target, i, cont) || cont(target, i)
    }
}

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
    val pattern = Alternative(
            Concat(Ch('m'),
                    Concat(Repeat(Ch('n')),
                            Concat(Optional(Ch('p')), Ch('a')))),
            Concat(Ch('m'), Ch('i'))
    )

    match(pattern, "mi")
    match(pattern, "mnnnnnnpa")
    match(pattern, "ma")
    match(pattern, "m")
    match(pattern, "konata");
}






