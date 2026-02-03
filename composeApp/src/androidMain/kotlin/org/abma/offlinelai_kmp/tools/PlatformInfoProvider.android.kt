package org.abma.offlinelai_kmp.tools

import android.os.Build

internal actual fun getPlatformInfoProvider(): PlatformInfoProvider = AndroidPlatformInfoProvider

private object AndroidPlatformInfoProvider : PlatformInfoProvider {
    override fun getPlatformName(): String = "Android"

    override fun getOsVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    override fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
}
