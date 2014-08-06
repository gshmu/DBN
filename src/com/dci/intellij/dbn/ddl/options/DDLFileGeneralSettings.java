package com.dci.intellij.dbn.ddl.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.options.setting.StringSetting;
import com.dci.intellij.dbn.ddl.options.ui.DDLFileGeneralSettingsForm;

public class DDLFileGeneralSettings extends Configuration<DDLFileGeneralSettingsForm> {
    private StringSetting statementPostfix = new StringSetting("statement-postfix", "/");
    private BooleanSetting lookupDDLFilesEnabled = new BooleanSetting("lookup-ddl-files", true);
    private BooleanSetting createDDLFilesEnabled = new BooleanSetting("create-ddl-files", false);

    public String getDisplayName() {
        return "DDL file general settings";
    }

    public StringSetting getStatementPostfix() {
        return statementPostfix;
    }

    public BooleanSetting getLookupDDLFilesEnabled() {
        return lookupDDLFilesEnabled;
    }

    public BooleanSetting getCreateDDLFilesEnabled() {
        return createDDLFilesEnabled;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public DDLFileGeneralSettingsForm createConfigurationEditor() {
        return new DDLFileGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    public void readConfiguration(Element element) {
        statementPostfix.readConfiguration(element);
        lookupDDLFilesEnabled.readConfiguration(element);
        createDDLFilesEnabled.readConfiguration(element);
    }

    public void writeConfiguration(Element element) {
        statementPostfix.writeConfiguration(element);
        lookupDDLFilesEnabled.writeConfiguration(element);
        createDDLFilesEnabled.writeConfiguration(element);
    }
}
