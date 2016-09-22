import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

/**
 * Created by anis on 9/2/16.
 */
public class PhoneGapInit extends AnAction {

    private final static Logger LOGGER = Logger.getLogger(PhoneGapInit.class.getName());
    private static boolean extracting = false;

    public PhoneGapInit() {
        super("Init _Cordova");
    }

    // used to disable Init Project
    public void update(AnActionEvent event) {
        super.update(event);

        event.getPresentation().setVisible(!extracting);

        Project project = event.getProject();

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module appModule = moduleManager.findModuleByName("app");

        OrderEntry[] deps  = ModuleRootManager.getInstance(appModule).getModifiableModel().getOrderEntries();
        for(OrderEntry m : deps) {
            if(m.getPresentableName().compareTo("cordova") == 0) {
                event.getPresentation().setVisible(false);
            }
        }
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        try {
            Path temp = Files.createTempFile("cordova-init-", ".zip");
            Files.copy(this.getClass().getClassLoader().getResourceAsStream("/resources/cordova-init.zip"), temp, StandardCopyOption.REPLACE_EXISTING);
            String destination = project.getBasePath();
            if (project == null) return;

            ProgressManager.getInstance().run(new Task.Backgroundable(project, "PhoneGap") {
                public void onSuccess() {
                    // adding Apache Cordova as a dependency to the "app" module
                    Application application = ApplicationManager.getApplication();
                    application.runWriteAction(() -> {
                        File cordovaJarFile = new File(project.getBasePath() + "/app/libs/cordova.jar");

                        ModuleManager moduleManager = ModuleManager.getInstance(project);
                        Module appModule = moduleManager.findModuleByName("app");

                        ModifiableRootModel moduleRootManager = ModuleRootManager.getInstance(appModule).getModifiableModel();
                        LibraryTable libTable = moduleRootManager.getModuleLibraryTable();
                        Library lib = libTable.createLibrary("cordova");

                        if (cordovaJarFile.exists() == false) {
                            LOGGER.info("Could not find Cordova JAR file");
                        }
                        Library.ModifiableModel libModel = lib.getModifiableModel();
                        libModel.addRoot(VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL,
                                cordovaJarFile.getPath() + JarFileSystem.JAR_SEPARATOR), OrderRootType.CLASSES);
                        libModel.commit();
                        moduleRootManager.commit();

                        // refresh project to see changes
                        project.getBaseDir().refresh(false, true);

                        Notification info = new Notification("PhoneGapInit", "You're rocking PhoneGap!", "PhoneGap was successfully added to your Android project", NotificationType.INFORMATION);
                        Notifications.Bus.notify(info);
                        extracting = false;

                    });
                }
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        Notification info = new Notification("PhoneGapInit", "Initializing PhoneGap", "Please be patient…", NotificationType.INFORMATION);
                        info.expire();
                        Notifications.Bus.notify(info);
                        extracting = true;
                        ZipUtils zipUtils = new ZipUtils();
                        zipUtils.unzip(temp.toFile(), destination, progressIndicator);
                    } catch (IOException e) {
                        LOGGER.severe("Can't unzip cordova-init for unknown reasons");
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
