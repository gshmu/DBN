package com.dci.intellij.dbn.language.common.lexer;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.intellij.psi.tree.IElementType;

public abstract class DBLanguageCompoundLexerBase extends DBLanguageLexerBase implements DBLanguageCompoundLexer {
    private final DBLanguageDialectIdentifier chameleonDialect;

    public DBLanguageCompoundLexerBase() {
        throw new UnsupportedOperationException();
    }

    protected DBLanguageCompoundLexerBase(TokenTypeBundle tt, DBLanguageDialectIdentifier chameleonDialect) {
        super(tt);
        this.chameleonDialect = chameleonDialect;
    }

    public IElementType getChameleon() {
        return tt.getChameleon(chameleonDialect);
    }
}
