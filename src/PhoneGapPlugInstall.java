import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.logging.Logger;

/**
 * Created by anis on 9/16/16.
 */
public class PhoneGapPlugInstall extends AnAction {

    private final static Logger LOGGER = Logger.getLogger(PhoneGapInit.class.getName());

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Messages.showMessageDialog(project, "Plugin Installation is not yet implemented", "Information", Messages.getInformationIcon());
    }
}
