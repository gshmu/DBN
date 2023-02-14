package com.dci.intellij.dbn.language.common.lexer;

import com.intellij.lexer.FlexLexer;

public interface DBLanguageFlexLexer extends FlexLexer {
    String getCurrentToken();
}
