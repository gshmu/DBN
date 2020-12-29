package com.dci.intellij.dbn.code.sql.style.options;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SQLCustomCodeStyleSettings extends CustomCodeStyleSettings {
    private final SQLCodeStyleSettings codeStyleSettings;

    SQLCustomCodeStyleSettings(CodeStyleSettings container) {
        super("SQLCodeStyleSettings", container);
        codeStyleSettings = new SQLCodeStyleSettings(null);
    }

    public SQLCodeStyleSettings getCodeStyleSettings() {
        return codeStyleSettings;
    }

    @Override
    public void readExternal(Element parentElement) throws InvalidDataException {
        codeStyleSettings.readConfiguration(parentElement);
    }

    @Override
    public void writeExternal(Element parentElement, @NotNull CustomCodeStyleSettings parentSettings) throws WriteExternalException {
        codeStyleSettings.writeConfiguration(parentElement);
    }
}
