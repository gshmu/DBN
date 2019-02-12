package com.dci.intellij.dbn.editor.code.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.ProjectSupplier;
import com.dci.intellij.dbn.editor.code.options.ui.CodeEditorGeneralSettingsForm;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class CodeEditorGeneralSettings extends Configuration<CodeEditorGeneralSettingsForm> implements ProjectSupplier {
    private boolean showObjectsNavigationGutter = false;
    private boolean showSpecDeclarationNavigationGutter = true;
    private boolean enableSpellchecking = true;
    private boolean enableReferenceSpellchecking = false;

    private CodeEditorSettings parent;

    public CodeEditorGeneralSettings(CodeEditorSettings parent) {
        this.parent = parent;
    }

    @Override
    public String getDisplayName() {
        return "Code editor general settings";
    }

    @Override
    public String getHelpTopic() {
        return "codeEditor";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public boolean isShowObjectsNavigationGutter() {
        return showObjectsNavigationGutter;
    }

    public void setShowObjectsNavigationGutter(boolean showObjectsNavigationGutter) {
        this.showObjectsNavigationGutter = showObjectsNavigationGutter;
    }

    public boolean isShowSpecDeclarationNavigationGutter() {
        return showSpecDeclarationNavigationGutter;
    }

    public void setShowSpecDeclarationNavigationGutter(boolean showSpecDeclarationNavigationGutter) {
        this.showSpecDeclarationNavigationGutter = showSpecDeclarationNavigationGutter;
    }

    public boolean isEnableSpellchecking() {
        return enableSpellchecking;
    }

    public void setEnableSpellchecking(boolean enableSpellchecking) {
        this.enableSpellchecking = enableSpellchecking;
    }

    public boolean isEnableReferenceSpellchecking() {
        return enableReferenceSpellchecking;
    }

    public void setEnableReferenceSpellchecking(boolean enableReferenceSpellchecking) {
        this.enableReferenceSpellchecking = enableReferenceSpellchecking;
    }

    @NotNull
    public Project getProject() {
        return parent.getProject();
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public CodeEditorGeneralSettingsForm createConfigurationEditor() {
        return new CodeEditorGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    @Override
    public void readConfiguration(Element element) {
        showObjectsNavigationGutter = SettingsUtil.getBoolean(element, "show-object-navigation-gutter", showObjectsNavigationGutter);
        showSpecDeclarationNavigationGutter = SettingsUtil.getBoolean(element, "show-spec-declaration-navigation-gutter", showSpecDeclarationNavigationGutter);
        enableSpellchecking = SettingsUtil.getBoolean(element, "enable-spellchecking", enableSpellchecking);
        enableReferenceSpellchecking = SettingsUtil.getBoolean(element, "enable-reference-spellchecking", enableReferenceSpellchecking);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "show-object-navigation-gutter", showObjectsNavigationGutter);
        SettingsUtil.setBoolean(element, "show-spec-declaration-navigation-gutter", showSpecDeclarationNavigationGutter);
        SettingsUtil.setBoolean(element, "enable-spellchecking", enableSpellchecking);
        SettingsUtil.setBoolean(element, "enable-reference-spellchecking", enableReferenceSpellchecking);
    }
}
