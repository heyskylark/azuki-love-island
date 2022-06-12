package com.heyskylark.azukiloveisland.exception

import java.lang.RuntimeException

class JsonSerializationException : RuntimeException {
    constructor()
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)
}
