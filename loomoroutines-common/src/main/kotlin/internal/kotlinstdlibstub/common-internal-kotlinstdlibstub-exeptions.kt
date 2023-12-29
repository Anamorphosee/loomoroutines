package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

class KotlinException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
