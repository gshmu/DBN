package com.dci.intellij.dbn.code.common.style.options.ui;

import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeStyleSettingsForm extends CompositeConfigurationEditorForm<ProjectCodeStyleSettings> {
    private JPanel mainPanel;
    private TabbedPane languageTabs;

    public CodeStyleSettingsForm(ProjectCodeStyleSettings settings) {
        super(settings);
        languageTabs = new TabbedPane(this);
        //languageTabs.setAdjustBorders(false);
        mainPanel.add(languageTabs, BorderLayout.CENTER);
        updateBorderTitleForeground(mainPanel);
        addSettingsPanel(getConfiguration().getSQLCodeStyleSettings(), Icons.FILE_SQL);
        addSettingsPanel(getConfiguration().getPSQLCodeStyleSettings(), Icons.FILE_PLSQL);
    }

    private void addSettingsPanel(Configuration configuration, Icon icon) {
        JComponent component = configuration.createComponent();
        TabInfo tabInfo = new TabInfo(component);
        tabInfo.setText(configuration.getDisplayName());
        tabInfo.setObject(configuration);
        tabInfo.setIcon(icon);
        languageTabs.addTab(tabInfo);
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
