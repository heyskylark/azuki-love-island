package com.heyskylark.azukiloveisland.util

// TODO: Fuzz test this with a bunch of random Twitter handles
fun String.isValidTwitterHandle(): Boolean {
    val regex = Regex("^[A-Za-z0-9_]{1,15}$")
    return this.matches(regex)
}
