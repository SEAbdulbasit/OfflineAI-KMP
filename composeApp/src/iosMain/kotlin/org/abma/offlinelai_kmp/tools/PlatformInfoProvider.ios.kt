package org.abma.offlinelai_kmp.tools

import platform.UIKit.UIDevice

internal actual fun getPlatformInfoProvider(): PlatformInfoProvider = IosPlatformInfoProvider

private object IosPlatformInfoProvider : PlatformInfoProvider {
    override fun getPlatformName(): String = "iOS"

    override fun getOsVersion(): String {
        val device = UIDevice.currentDevice
        return "iOS ${device.systemVersion}"
    }

    override fun getDeviceModel(): String {
        return UIDevice.currentDevice.model
    }
}
