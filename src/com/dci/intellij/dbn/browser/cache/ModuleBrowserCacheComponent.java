package com.dci.intellij.dbn.browser.cache;

import com.dci.intellij.dbn.connection.ModuleConnectionBundle;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ModuleBrowserCacheComponent extends BrowserCacheComponent implements ModuleComponent {

    private final Module module;
    private ModuleBrowserCacheComponent(Module module) {
        super( createModuleConfigFile(module),
               module.getComponent(ModuleConnectionBundle.class));
        this.module = module;
    }


    public static ModuleBrowserCacheComponent getInstance(Module module) {
        return new ModuleBrowserCacheComponent(module);
    }

    private static File createModuleConfigFile(Module module) {
        VirtualFile virtualFile = module.getModuleFile();
        if (virtualFile != null) {
            String path = module.getModuleFile().getPath();
            String name = module.getModuleFile().getNameWithoutExtension() + FILE_EXTENSION;
            return new File(path, name);
        }
        return null;
    }


    /***************************************
    *            ModuleComponent           *
    ****************************************/
    @NonNls @NotNull
    public String getComponentName() {
        return "DBNavigator.Module.BrowserCacheComponent";
    }

    public void projectOpened() {}
    public void projectClosed() {}
    public void moduleAdded() {}
    public void initComponent() {}
    public void disposeComponent() {}
}
