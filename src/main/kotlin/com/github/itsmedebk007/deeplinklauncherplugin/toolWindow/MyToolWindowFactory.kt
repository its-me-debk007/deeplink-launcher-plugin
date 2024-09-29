package com.github.itsmedebk007.deeplinklauncherplugin.toolWindow

import com.github.itsmedebk007.deeplinklauncherplugin.services.MyProjectService
import com.github.itsmedebk007.deeplinklauncherplugin.util.NotificationUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.UIUtil
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.awt.Font
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

                    NotificationUtil.showInfoNotification("Deeplink launched successfully")
                }
            }

            add(heading)
            add(textField)
            add(button)

            val adb = AndroidSdkUtils.getDebugBridge(toolWindow.project)

            adb?.let {
                it.devices.forEach { device ->
                    val button = JButton(device.name)
                    add(button)

//                    val coroutineScope = CoroutineScope(Dispatchers.IO)
//                    coroutineScope.launch {
//                        device.runShellCommand("am start -a android.intent.action.VIEW -d \"${textField.text}\"")
//                    }
                }
            }
        }
    }
}
