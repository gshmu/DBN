package com.dci.intellij.dbn.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;

import javax.swing.Icon;


public abstract class DBModuleWizardStep extends ModuleWizardStep {
    public DBModuleWizardStep(WizardContext wizardContext, ModuleBuilder moduleBuilder, ModulesProvider modulesProvider) {
        this.wizardContext = wizardContext;
        this.moduleBuilder = moduleBuilder;
        this.modulesProvider = modulesProvider;
    }

    protected WizardContext wizardContext;
    protected ModuleBuilder moduleBuilder;
    protected ModulesProvider modulesProvider;

    public DBModuleBuilder getModuleBuilder() {
        return (DBModuleBuilder) moduleBuilder;
    }

    public Icon getIcon() {
        return ICON;
    }



}
