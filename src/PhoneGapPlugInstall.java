import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.ui.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Created by anis on 9/16/16.
 */
public abstract class PhoneGapPlugInstall extends AnAction {

    private final static Logger LOGGER = Logger.getLogger(PhoneGapInit.class.getName());
    private static String OS = null;

    public static String getOsName() {
        if (OS == null) {
            OS = System.getProperty("os.name");
        }
        return OS;
    }

    protected void executeCommand(String command) {
        StringBuffer output = new StringBuffer();

        Process p;

        try {
            p = Runtime.getRuntime().exec(command);

            new Thread(() -> {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        System.out.println(line);
                        output.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line;
                    try {
                        while ((line = input.readLine()) != null) {
                            output.append(line + "\n");
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
            }).start();

            int exitCode = p.waitFor();

            Notification postInstall;
            if(exitCode != 0) {
                postInstall = new Notification("error", "Error installing plugin", output.toString(), NotificationType.ERROR);
            } else {
                postInstall = new Notification("success", "Plugin installed successfully!", output.toString(), NotificationType.INFORMATION);
            }
            Notifications.Bus.notify(postInstall);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean checkEnvironment() {
        Process node, plugman;

        String nodeCmdStr = "which node",
                plugmanCmdStr = "which plugman";

        if (getOsName().startsWith("Windows")) {
            nodeCmdStr = "where node";
            plugmanCmdStr = "where plugman";
        }

        try {
            node = Runtime.getRuntime().exec(nodeCmdStr);
            node.waitFor();
            if (node.exitValue() != 0) {
                return false;
            }

            plugman = Runtime.getRuntime().exec(plugmanCmdStr);
            plugman.waitFor();
            if (plugman.exitValue() != 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // used to disable Install Plugin
    public void update(AnActionEvent event) {
        super.update(event);

        Application application = ApplicationManager.getApplication();
        application.runReadAction(() -> {
            Project project = event.getProject();

            ModuleManager moduleManager = ModuleManager.getInstance(project);
            Module appModule = moduleManager.findModuleByName("app");

            ModifiableRootModel moduleRootManager = ModuleRootManager.getInstance(appModule).getModifiableModel();
            LibraryTable libTable = moduleRootManager.getModuleLibraryTable();
            Library phonegapLib = libTable.getLibraryByName("phonegap");
            if(phonegapLib == null) {
                event.getPresentation().setVisible(false);
            } else {
                event.getPresentation().setVisible(true);
            }

        });
    }

    public abstract void install(Project project);

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (checkEnvironment() == false) {
            Messages.showMessageDialog(project, "You need to install NodeJS and plugman in order to be able to use plugins", "Error", Messages.getErrorIcon());
            return;
        }
        install(project);
        project.getBaseDir().refresh(false, true);
    }
}
