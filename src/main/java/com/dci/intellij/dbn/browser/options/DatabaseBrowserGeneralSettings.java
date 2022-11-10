package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserGeneralSettingsForm;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.options.setting.IntegerSetting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getEnum;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setEnum;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DatabaseBrowserGeneralSettings
        extends BasicProjectConfiguration<DatabaseBrowserSettings, DatabaseBrowserGeneralSettingsForm> {

    private BrowserDisplayMode displayMode = BrowserDisplayMode.TABBED;
    private final IntegerSetting navigationHistorySize = new IntegerSetting("navigation-history-size", 100);
    private final BooleanSetting showObjectDetails = new BooleanSetting("show-object-details", false);

    DatabaseBrowserGeneralSettings(DatabaseBrowserSettings parent) {
        super(parent);
    }

    @NotNull
    @Override
    public DatabaseBrowserGeneralSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    @Override
    public void readConfiguration(Element element) {
        displayMode = getEnum(element, "display-mode", BrowserDisplayMode.TABBED);
        if (displayMode == BrowserDisplayMode.SINGLE) displayMode = BrowserDisplayMode.SIMPLE;
        navigationHistorySize.readConfiguration(element);
        showObjectDetails.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        setEnum(element, "display-mode", displayMode);
        navigationHistorySize.writeConfiguration(element);
        showObjectDetails.writeConfiguration(element);
    }

}
