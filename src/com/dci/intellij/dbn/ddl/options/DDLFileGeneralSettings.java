package com.dci.intellij.dbn.ddl.options;

import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.ddl.options.ui.DDLFileGeneralSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DDLFileGeneralSettings extends BasicProjectConfiguration<DDLFileSettings, DDLFileGeneralSettingsForm> {
    private BooleanSetting lookupDDLFilesEnabled = new BooleanSetting("lookup-ddl-files", true);
    private BooleanSetting createDDLFilesEnabled = new BooleanSetting("create-ddl-files", false);
    private BooleanSetting synchronizeDDLFilesEnabled = new BooleanSetting("synchronize-ddl-files", true);
    private BooleanSetting useQualifiedObjectNames = new BooleanSetting("use-qualified-names", false);
    private BooleanSetting makeScriptsRerunnable = new BooleanSetting("make-scripts-rerunnable", true);

    DDLFileGeneralSettings(DDLFileSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "DDL file general settings";
    }

    public BooleanSetting getLookupDDLFilesEnabled() {
        return lookupDDLFilesEnabled;
    }

    public boolean isLookupDDLFilesEnabled() {
        return lookupDDLFilesEnabled.value();
    }

    public BooleanSetting getCreateDDLFilesEnabled() {
        return createDDLFilesEnabled;
    }

    public boolean isCreateDDLFilesEnabled() {
        return createDDLFilesEnabled.value();
    }

    public boolean isSynchronizeDDLFilesEnabled() {
        return synchronizeDDLFilesEnabled.value();
    }
    public BooleanSetting getSynchronizeDDLFilesEnabled() {
        return synchronizeDDLFilesEnabled;
    }

    public BooleanSetting getUseQualifiedObjectNames() {
        return useQualifiedObjectNames;
    }

    public boolean isUseQualifiedObjectNames() {
        return useQualifiedObjectNames.value();
    }

    public BooleanSetting getMakeScriptsRerunnable() {
        return makeScriptsRerunnable;
    }

    public boolean isMakeScriptsRerunnable() {
        return makeScriptsRerunnable.value();
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public DDLFileGeneralSettingsForm createConfigurationEditor() {
        return new DDLFileGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    @Override
    public void readConfiguration(Element element) {
        lookupDDLFilesEnabled.readConfiguration(element);
        createDDLFilesEnabled.readConfiguration(element);
        synchronizeDDLFilesEnabled.readConfiguration(element);
        useQualifiedObjectNames.readConfiguration(element);
        makeScriptsRerunnable.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        lookupDDLFilesEnabled.writeConfiguration(element);
        createDDLFilesEnabled.writeConfiguration(element);
        synchronizeDDLFilesEnabled.writeConfiguration(element);
        useQualifiedObjectNames.writeConfiguration(element);
        makeScriptsRerunnable.writeConfiguration(element);
    }
}
