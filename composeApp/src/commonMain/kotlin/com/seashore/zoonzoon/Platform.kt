package com.seashore.zoonzoon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform