package com.dci.intellij.dbn.code.common.completion.options.general;

import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.completion.options.general.ui.CodeCompletionFormatSettingsForm;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBoolean;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBoolean;

public class CodeCompletionFormatSettings extends BasicConfiguration<CodeCompletionSettings, CodeCompletionFormatSettingsForm> {
    private boolean enforceCodeStyleCase = true;

    public CodeCompletionFormatSettings(CodeCompletionSettings parent) {
        super(parent);
    }

    public boolean isEnforceCodeStyleCase() {
        return enforceCodeStyleCase;
    }

    public void setEnforceCodeStyleCase(boolean enforceCodeStyleCase) {
        this.enforceCodeStyleCase = enforceCodeStyleCase;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
   @Override
   @NotNull
   public CodeCompletionFormatSettingsForm createConfigurationEditor() {
       return new CodeCompletionFormatSettingsForm(this);
   }

    @Override
    public String getConfigElementName() {
        return "format";
    }

    @Override
    public void readConfiguration(Element element) {
        enforceCodeStyleCase = getBoolean(element, "enforce-code-style-case", enforceCodeStyleCase);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "enforce-code-style-case", enforceCodeStyleCase);
    }

}
