package com.dci.intellij.dbn.code.common.completion.options.general;

import com.dci.intellij.dbn.code.common.completion.options.general.ui.CodeCompletionFormatSettingsForm;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class CodeCompletionFormatSettings extends Configuration<CodeCompletionFormatSettingsForm>{
    private boolean enforceCodeStyleCase = true;

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
        enforceCodeStyleCase = SettingsUtil.getBoolean(element, "enforce-code-style-case", enforceCodeStyleCase);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "enforce-code-style-case", enforceCodeStyleCase);
    }

}
