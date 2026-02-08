package org.abma.offlinelai_kmp.tools

interface PlatformInfoProvider {
    fun getPlatformName(): String
    fun getOsVersion(): String
    fun getDeviceModel(): String

    companion object : PlatformInfoProvider by getPlatformInfoProvider()
}

internal expect fun getPlatformInfoProvider(): PlatformInfoProvider
