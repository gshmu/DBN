package com.dci.intellij.dbn.plugin;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.file.FileTypeService;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

import static com.dci.intellij.dbn.common.component.Components.applicationService;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBoolean;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBoolean;
import static com.dci.intellij.dbn.plugin.PluginConflictManager.COMPONENT_NAME;

@Getter
@Setter
@State(
    name = COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class PluginConflictManager extends ApplicationComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Application.PluginConflictManager";
    public static final @NotNull PluginId JETBRAINS_DB_PLUGIN_ID = PluginId.getId("com.intellij.database");
    private boolean fileTypesClaimed;

    public PluginConflictManager() {
        super(COMPONENT_NAME);
    }

    public static PluginConflictManager getInstance() {
        return applicationService(PluginConflictManager.class);
    }


    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        setBoolean(element, "file-types-claimed", fileTypesClaimed);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        fileTypesClaimed = getBoolean(element, "file-types-claimed", false);
    }

    @SneakyThrows
    public boolean isJetbrainsDbPluginInstalled() {
        IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(JETBRAINS_DB_PLUGIN_ID);
        if (pluginDescriptor == null) return false;

        try {
            ClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
            if (pluginClassLoader == null) return false;

            Class<?> psiFacadeClass = pluginClassLoader.loadClass("com.intellij.database.psi.DbPsiFacade");
            Method getInstanceMethod = psiFacadeClass.getMethod("getInstance", Project.class);

        } catch (Throwable ignore) {}

        return false;
    }

    @Override
    public void initializeComponent() {
        // TODO prompt
        if (fileTypesClaimed) return;
        fileTypesClaimed = true;
        if (isJetbrainsDbPluginInstalled()) return;

        FileTypeService fileTypeService = FileTypeService.getInstance();
        fileTypeService.claimFileAssociations(SQLFileType.INSTANCE);
        fileTypeService.claimFileAssociations(PSQLFileType.INSTANCE);
    }

}
