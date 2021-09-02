package com.dci.intellij.dbn.editor.code.options;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.dci.intellij.dbn.editor.code.options.ui.CodeEditorGeneralSettingsForm;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CodeEditorGeneralSettings
        extends BasicConfiguration<CodeEditorSettings, CodeEditorGeneralSettingsForm>
        implements ProjectSupplier {

    private boolean showObjectsNavigationGutter = false;
    private boolean showSpecDeclarationNavigationGutter = true;
    private boolean enableSpellchecking = true;
    private boolean enableReferenceSpellchecking = false;

    CodeEditorGeneralSettings(CodeEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Code editor general settings";
    }

    @Override
    public String getHelpTopic() {
        return "codeEditor";
    }

    @NotNull
    public Project getProject() {
        return getParent().getProject();
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
        showObjectsNavigationGutter = SettingsSupport.getBoolean(element, "show-object-navigation-gutter", showObjectsNavigationGutter);
        showSpecDeclarationNavigationGutter = SettingsSupport.getBoolean(element, "show-spec-declaration-navigation-gutter", showSpecDeclarationNavigationGutter);
        enableSpellchecking = SettingsSupport.getBoolean(element, "enable-spellchecking", enableSpellchecking);
        enableReferenceSpellchecking = SettingsSupport.getBoolean(element, "enable-reference-spellchecking", enableReferenceSpellchecking);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setBoolean(element, "show-object-navigation-gutter", showObjectsNavigationGutter);
        SettingsSupport.setBoolean(element, "show-spec-declaration-navigation-gutter", showSpecDeclarationNavigationGutter);
        SettingsSupport.setBoolean(element, "enable-spellchecking", enableSpellchecking);
        SettingsSupport.setBoolean(element, "enable-reference-spellchecking", enableReferenceSpellchecking);
    }
}
