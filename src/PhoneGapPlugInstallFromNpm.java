import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * Created by anis on 9/16/16.
 */
public class PhoneGapPlugInstallFromNpm extends PhoneGapPlugInstall {

    @Override
    public void install(Project project) {
        String pluginName = Messages.showInputDialog(project, "Enter plugin name (select from http://cordova.apache.org/plugins/)",
                "Which Plugin ?", Messages.getQuestionIcon());
        Notification info = new Notification("info", "Installing plugin " + pluginName,
                "Please be patient…", NotificationType.INFORMATION);
        info.expire();
        Notifications.Bus.notify(info, project);
        System.out.println("Executing "+String.format("plugman install --platform android --plugin %s --project %s", pluginName, project.getBasePath() + "/"));
        executeCommand(String.format("plugman install --platform android --plugin %s --project %s", pluginName, project.getBasePath()));
    }
}
