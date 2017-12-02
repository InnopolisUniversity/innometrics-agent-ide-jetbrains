package ru.innopolis.university.innometrics.jb_plugin.actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBTextField;
import ru.innopolis.university.innometrics.jb_plugin.services.ActivitiesSenderService;

import javax.swing.*;

public class ToolsMenuItem extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        String innometricsLogin = PropertiesComponent.getInstance().getValue("innometrics.login", "");
        String innometricsPassword = PropertiesComponent.getInstance().getValue("innometrics.password", "");
        String innometricsUrl = PropertiesComponent.getInstance().getValue("innometrics.url", "http://127.0.0.1:8000/");

        JTextField username = new JBTextField(innometricsLogin);
        JPasswordField password = new JPasswordField(innometricsPassword);
        JTextField url = new JBTextField(innometricsUrl);
        Object[] message = {
                "Username:", username,
                "Password:", password,
                "Url:", url
        };

        // TODO replace with DialogWrapper http://www.jetbrains.org/intellij/sdk/docs/user_interface_components/dialog_wrapper.html
        int option = JOptionPane.showConfirmDialog(null, message, "Innometrics settings", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            PropertiesComponent.getInstance().setValue("innometrics.login", username.getText());
            PropertiesComponent.getInstance().setValue("innometrics.url", url.getText());
            PropertiesComponent.getInstance().setValue("innometrics.password", new String(password.getPassword()));
            int sendOption = Messages.showOkCancelDialog(project, "Send measurements?", "Innometrics Sending Data", Messages.getQuestionIcon());
            if (sendOption == Messages.OK) {
                ServiceManager.getService(ActivitiesSenderService.class).sendMeasurements();
            }
        }/* else {
            return;
        }*/
    }
}
