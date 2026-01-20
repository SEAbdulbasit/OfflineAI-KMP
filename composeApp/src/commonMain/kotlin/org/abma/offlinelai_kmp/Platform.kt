package org.abma.offlinelai_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform