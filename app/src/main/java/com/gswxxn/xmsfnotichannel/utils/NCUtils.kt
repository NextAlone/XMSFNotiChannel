package com.gswxxn.xmsfnotichannel.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.IBinder
import org.lsposed.hiddenapibypass.HiddenApiBypass


@SuppressLint("PrivateApi", "SoonBlockedPrivateApi", "DiscouragedPrivateApi")
class NCUtils(private val context: Context) {

    private val sINM by lazy {
        val notificationService = Class.forName("android.os.ServiceManager").getDeclaredMethod(
            "getService",
            String::class.java
        ).invoke(null, "notification")

        Class.forName("android.app.INotificationManager\$Stub")
            .getDeclaredMethod("asInterface", IBinder::class.java)
            .invoke(null, notificationService)
    }

    private val notificationChannelGroupsM = HiddenApiBypass.getDeclaredMethod(
        sINM.javaClass,
        "getNotificationChannelGroupsForPackage",
        String::class.java,
        Int::class.java,
        Boolean::class.java
    )

    private val updateNotificationChannelForPackageM = HiddenApiBypass.getDeclaredMethod(
        sINM.javaClass,
        "updateNotificationChannelForPackage",
        String::class.java,
        Int::class.java,
        NotificationChannel::class.java
    )

    private fun getNotificationChannelGroups(pkgName: String): List<NotificationChannelGroup> {
        return try {
            notificationChannelGroupsM.invoke(
                sINM,
                pkgName,
                context.packageManager.getPackageUid(pkgName, 0),
                false
            )?.let { parceledListSlice ->
                HiddenApiBypass.getDeclaredMethod(
                    parceledListSlice.javaClass,
                    "getList"
                ).invoke(parceledListSlice) as? List<NotificationChannelGroup>
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun setNotificationChannel(pkgName: String, channel: NotificationChannel): Any? =
        updateNotificationChannelForPackageM.invoke(
            sINM, pkgName, context.packageManager.getPackageUid(pkgName, 0), channel
        )

    // 获取通知通道信息
    fun getNotificationChannelInfoByRegex(
        pkgName: String,
        channelNameRegex: String
    ): List<AppInfoHelper.NCInfo> {
        val groups = getNotificationChannelGroups(pkgName)
        val result = groups
            .flatMap { group -> group.channels.map { it to group } }
            .filter { Regex(channelNameRegex).containsMatchIn(it.first.name) }
            .map {
                AppInfoHelper.NCInfo(
                    it.second.name?.toString() ?: "",
                    it.first.name.toString(),
                    it.first.importance
                )
            }
        return result
    }

    fun enableSpecificNotification(appInfo: AppInfoHelper.MyAppInfo) {
        getNotificationChannelGroups(appInfo.packageName).forEach { group ->
            group.channels.forEach { channel ->
                if (channel.name == appInfo.ncInfo.channelName) {
                    channel.importance = NotificationManager.IMPORTANCE_DEFAULT
                    setNotificationChannel(appInfo.packageName, channel)
                }
            }
        }
    }

    fun disableSpecificNotification(appInfo: AppInfoHelper.MyAppInfo) {
        getNotificationChannelGroups(appInfo.packageName).forEach { group ->
            group.channels.forEach { channel ->
                if (channel.name == appInfo.ncInfo.channelName) {
                    channel.importance = NotificationManager.IMPORTANCE_NONE
                    setNotificationChannel(appInfo.packageName, channel)
                }
            }
        }
    }
}