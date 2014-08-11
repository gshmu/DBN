package com.dci.intellij.dbn.module;

import javax.swing.JComponent;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;

public class DBModuleConnectionsWizardStep extends DBModuleWizardStep {

    public DBModuleConnectionsWizardStep(WizardContext wizardContext, ModuleBuilder moduleBuilder, ModulesProvider modulesProvider) {
        super(wizardContext, moduleBuilder, modulesProvider);
    }

    public JComponent getComponent() {
        return getModuleBuilder().createConnectionManagerComponent();
    }

    public void updateDataModel() {

    }
}
