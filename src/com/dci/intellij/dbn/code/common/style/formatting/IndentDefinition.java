package com.dci.intellij.dbn.code.common.style.formatting;

import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.intellij.formatting.Indent;
import org.jdom.Element;

public enum IndentDefinition implements FormattingAttribute<Indent> {
    NORMAL       (new Loader(){
        @Override
        Indent load() {return Indent.getNormalIndent(true);}}),
    CONTINUE     (new Loader(){
        @Override
        Indent load() {return Indent.getContinuationIndent();}}),
    NONE         (new Loader(){
        @Override
        Indent load() {return Indent.getNoneIndent();}}),
    ABSOLUTE_NONE(new Loader(){
        @Override
        Indent load() {return Indent.getAbsoluteNoneIndent();}});

    private Indent value;
    private Loader<Indent> loader;

    private IndentDefinition(Loader<Indent> loader) {
        this.loader = loader;
    }

    @Override
    public Indent getValue() {
        if (value == null && loader != null) {
            value = loader.load();
            loader = null;
        }
        return value;
    }

    public static IndentDefinition get(Element element) {
        return SettingsSupport.getEnumAttribute(element, "formatting-indent", IndentDefinition.class);
    }
}
