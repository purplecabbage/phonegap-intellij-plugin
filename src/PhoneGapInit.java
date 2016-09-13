import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import icons.PhoneGapIcons;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Created by anis on 9/2/16.
 */
public class PhoneGapInit extends AnAction {

    private final static Logger LOGGER = Logger.getLogger(PhoneGapInit.class.getName());

    public PhoneGapInit() {
        super("Init _Cordova");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        try {
            File file = new File(this.getClass().getClassLoader().getResource("/resources/cordova-init.zip").toURI());
            String source = file.toString();
            String destination = project.getBasePath();
            if(project == null) return;
            ZipUtils zipUtils = new ZipUtils();
            zipUtils.unzip(source, destination);

            // adding Apache Cordova as a dependency to the "app" module
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(() -> {
                File cordovaJarFile = new File(project.getBasePath() + "/app/libs/cordova-5.2.0-dev.jar");

                ModuleManager moduleManager = ModuleManager.getInstance(project);
                Module appModule = moduleManager.findModuleByName("app");

                ModifiableRootModel moduleRootManager = ModuleRootManager.getInstance(appModule).getModifiableModel();
                LibraryTable libTable = moduleRootManager.getModuleLibraryTable();
                Library lib = libTable.createLibrary("phonegap");

                if(cordovaJarFile.exists() == false) {
                    LOGGER.info("Could not find Cordova JAR file");
                }
                Library.ModifiableModel libModel = lib.getModifiableModel();
                libModel.addRoot(VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL,
                        cordovaJarFile.getPath() + JarFileSystem.JAR_SEPARATOR), OrderRootType.CLASSES);
                libModel.commit();
                moduleRootManager.commit();
                Messages.showMessageDialog(project, "PhoneGap Project successfully initialized!", "Information", PhoneGapIcons.PHONEGAP_INFO);
            });
        } catch(URISyntaxException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
