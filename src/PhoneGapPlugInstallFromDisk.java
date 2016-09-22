import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by anis on 9/21/16.
 */
public class PhoneGapPlugInstallFromDisk extends PhoneGapPlugInstall {

    @Override
    public void install(Project project) {
        FileChooserDescriptor fcd = new FileChooserDescriptor(false, true, false, false, false, false);
        fcd.setDescription("Please select PhoneGap/Cordova plugin folder");
        fcd.setTitle("Select PhoneGap Plugin");
        VirtualFile pluginDir = FileChooser.chooseFile(fcd, project, null);
        if (pluginDir != null) {
            Notification info = new Notification("info", "Installing plugin " + pluginDir.getPath(),
                    "Please be patient…", NotificationType.INFORMATION);
            info.expire();
            Notifications.Bus.notify(info, project);
            executeCommand(String.format("plugman install --platform android --plugin %s --project %s",
                    pluginDir.getPath(), project.getBasePath()));
        }
    }
}
