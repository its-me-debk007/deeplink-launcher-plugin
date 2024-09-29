package com.github.itsmedebk007.deeplinklauncherplugin.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

object NotificationUtil {
    private const val TITLE = "Deeplink Launcher"
    private const val NOTIFICATION_GROUP = "DEEPLINK_LAUNCHER_GROUP"

    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup(NOTIFICATION_GROUP)

    fun showInfoNotification(message: String) {
        val notification = notificationGroup.createNotification(
            title = TITLE,
            content = message,
            type = NotificationType.INFORMATION
        )
        notification.notify(null)
    }
}