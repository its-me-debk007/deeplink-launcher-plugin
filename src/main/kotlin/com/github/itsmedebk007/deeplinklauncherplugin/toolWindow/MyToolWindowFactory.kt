package com.github.itsmedebk007.deeplinklauncherplugin.toolWindow

import com.android.ddmlib.IDevice
import com.android.tools.idea.wearpairing.runShellCommand
import com.github.itsmedebk007.deeplinklauncherplugin.services.DataPersistentComponent
import com.github.itsmedebk007.deeplinklauncherplugin.util.NotificationUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ItemEvent
import java.net.URI
import javax.swing.JButton

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {

        private val dataPersistentComponent by lazy { toolWindow.project.service<DataPersistentComponent>() }
        val adb by lazy { AndroidSdkUtils.getDebugBridge(toolWindow.project) }
        private val devicesList = mutableListOf<IDevice>()

        fun getContent() = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT)).apply {
            val customCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            Disposer.register(toolWindow.disposable, Disposable {
                customCoroutineScope.cancel()
            })

            val deeplinkList = dataPersistentComponent.state

            val heading = JBLabel("Enter deeplink below:").apply {
                font = font.deriveFont(Font.BOLD)
            }
            val textField = JBTextField(20)
            val button = JButton("Launch").apply {
                addActionListener {
//                    val facets = ProjectFacetManager.getInstance(toolWindow.project).getFacets(AndroidFacet.ID)
//                    val packageName = AndroidModel.get(facets[0])?.applicationId
                    customCoroutineScope.launch {
                        devicesList.launchDeepLink(
                            deeplink = textField.text,
                            onSuccess = {
                                NotificationUtil.showInfoNotification("Deeplink launched successfully")
                                if (deeplinkList.deeplinks.contains(textField.text).not()) {
                                    deeplinkList.deeplinks.add(textField.text)
                                    dataPersistentComponent.loadState(deeplinkList)
                                    listModel.addElement(textField.text)
                                }
                                textField.text = ""
                            },
                            onFailure = {
                                NotificationUtil.showErrorNotification("Incorrect deeplink")
                            }
                        )
                    }
                }
            }

            add(heading)
            add(textField)
            add(button)

            adb?.let {
                it.devices.forEach { device ->
                    val deviceCheckbox = JBCheckBox(device.name).apply {
                        isSelected = true
                        devicesList.add(device)

                        addItemListener { event ->
                            if (event.stateChange == ItemEvent.SELECTED) devicesList.add(device)
                            else devicesList.remove(device)
                        }
                    }
                    add(deviceCheckbox)
                }
            }

            dataPersistentComponent.state.deeplinks.forEach { deeplink ->
                val deeplinkBtn = JButton(deeplink).apply {
                    addActionListener {
                        devicesList.launchDeepLink(deeplink)
                    }
                }

        private suspend fun List<IDevice>.launchDeepLink(
            deeplink: String,
            onSuccess: () -> Unit,
            onFailure: () -> Unit
        ) {
            if (deeplink.isBlank()) return

            runCatching { URI(deeplink.trim()) }
                .onFailure {
                    NotificationUtil.showErrorNotification("Incorrect deeplink")
                    return
                }

            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                this@launchDeepLink.forEach { device ->
                    device.runShellCommand("am start -a android.intent.action.VIEW -d \"${deeplink.trim()}\"")
                }

                NotificationUtil.showInfoNotification("Deeplink launched successfully")
            }
        }
    }
}
