package com.dci.intellij.dbn.code.psql.style.options;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(callSuper = false)
public class PSQLCustomCodeStyleSettings extends CustomCodeStyleSettings {
    private final PSQLCodeStyleSettings codeStyleSettings;

    PSQLCustomCodeStyleSettings(CodeStyleSettings container) {
        super("PSQLCodeStyleSettings", container);
        codeStyleSettings = new PSQLCodeStyleSettings(null);
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