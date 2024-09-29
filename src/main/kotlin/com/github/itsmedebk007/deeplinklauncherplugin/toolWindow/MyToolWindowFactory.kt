package com.github.itsmedebk007.deeplinklauncherplugin.toolWindow

import com.android.ddmlib.IDevice
import com.android.tools.idea.wearpairing.runShellCommand
import com.github.itsmedebk007.deeplinklauncherplugin.services.MyProjectService
import com.github.itsmedebk007.deeplinklauncherplugin.util.NotificationUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.awt.Font
import java.awt.event.ItemEvent
import javax.swing.JButton

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()
        private val devicesList = mutableListOf<IDevice>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val heading = JBLabel("Enter deeplink here").apply {
                this.font = this.font.deriveFont(Font.BOLD)
                this.fontColor = UIUtil.FontColor.BRIGHTER
            }
            val textField = JBTextField(20)
            val button = JButton("Launch").apply {
                addActionListener {
//                    val facets = ProjectFacetManager.getInstance(toolWindow.project).getFacets(AndroidFacet.ID)
//                    val packageName = AndroidModel.get(facets[0])?.applicationId

                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    coroutineScope.launch {
                        devicesList.forEach { device ->
                            device.runShellCommand("am start -a android.intent.action.VIEW -d \"${textField.text}\"")
                        }
                    }

                    NotificationUtil.showInfoNotification("Deeplink launched successfully")
                }
            }

            add(heading)
            add(textField)
            add(button)

            val adb = AndroidSdkUtils.getDebugBridge(toolWindow.project)

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
        }
    }
}
