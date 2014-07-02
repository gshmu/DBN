package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ModuleConnectionBundle extends ConnectionBundle implements ModuleComponent, Configurable {

    private Module module;
    public ModuleConnectionBundle(Module module) {
        super(module == null ? null : module.getProject());
        this.module = module;
    }

    public static ModuleConnectionBundle getInstance(Module module) {
        return module.getComponent(ModuleConnectionBundle.class);
    }

    public Module getModule() {
        return module;
    }

    public Icon getIcon(int flags) {
        ModuleType moduleType = CompatibilityUtil.getModuleType(module);
        return module == null ? null : moduleType.getNodeIcon(false);
    }

    /***************************************
    *            Configurable              *
    ****************************************/
    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Module.ConnectionBundle";
    }

    @Nls
    public String getDisplayName() {
        return "DB Connections";
    }

    public Icon getIcon() {
        return Icons.CONNECTIONS;
    }

    /***************************************
    *            ModuleComponent           *
    ****************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Module.ConnectionManager";
    }

    public void projectOpened() {}
    public void projectClosed() {}
    public void moduleAdded() {}
    public void initComponent() {}
    public void disposeComponent() {
        dispose();
    }


    @Override
    public String toString() {
        return "ModuleConnectionBundle[" + getModule().getName() + "]";
    }

    public int compareTo(@NotNull Object o) {
        if (o instanceof ModuleConnectionBundle) {
            ModuleConnectionBundle connectionManager = (ModuleConnectionBundle) o;
            return getModule().getName().compareTo(connectionManager.getModule().getName());
        }
        return -1;
    }
}
