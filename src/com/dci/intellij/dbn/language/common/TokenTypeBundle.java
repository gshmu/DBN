package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.intellij.psi.tree.TokenSet;
import org.jdom.Element;

public class TokenTypeBundle extends TokenTypeBundleBase {
    private final DBLanguage language;

    public TokenTypeBundle(DBLanguageDialect languageDialect, Element root) {
        super(languageDialect, root);
        language = languageDialect.getBaseLanguage();
        initIndex(getSharedTokenTypes().size());
    }

    public TokenType getTokenType(int index) {
        TokenType tokenType = super.getTokenType(index);
        if (tokenType == null ){
            return getSharedTokenTypes().getTokenType(index);
        }
        return tokenType;
    }

    public SharedTokenTypeBundle getSharedTokenTypes() {
        return language.getSharedTokenTypes();
    }

    public DBLanguageDialect getLanguageDialect() {
        return (DBLanguageDialect) getLanguage();
    }

    @Override
    public SimpleTokenType getCharacterTokenType(int index) {
        return getSharedTokenTypes().getCharacterTokenType(index);
    }

    @Override
    public SimpleTokenType getOperatorTokenType(int index) {
        return getSharedTokenTypes().getOperatorTokenType(index);
    }

    @Override
    public SimpleTokenType getTokenType(String id) {
        SimpleTokenType tokenType = super.getTokenType(id);
        if (tokenType == null) {
            tokenType = getSharedTokenTypes().getTokenType(id);
            if (tokenType == null) {
                System.out.println("DEBUG - [" + getLanguage().getID() + "] undefined token type: " + id);
                //log.info("[DBN-WARNING] Undefined token type: " + id);
                return getSharedTokenTypes().getIdentifier();
            }
        }
        return tokenType;
    }

    @Override
    public TokenSet getTokenSet(String id) {
        TokenSet tokenSet = super.getTokenSet(id);
        if (tokenSet == null) {
            tokenSet = getSharedTokenTypes().getTokenSet(id);
            if (tokenSet == null) {
                System.out.println("DEBUG - [" + getLanguage().getID() + "] undefined token set: " + id);
                //log.info("[DBN-WARNING] Undefined token set '" + id + "'");
                tokenSet = super.getTokenSet("UNDEFINED");
            }
        }
        return tokenSet;
    }

    public SimpleTokenType getIdentifier() {
        return getSharedTokenTypes().getIdentifier();
    }

    public SimpleTokenType getVariable() {
        return getSharedTokenTypes().getVariable();
    }

    public SimpleTokenType getString() {
        return getSharedTokenTypes().getString();
    }


    public ChameleonElementType getChameleon(DBLanguageDialectIdentifier dialectIdentifier) {
        return getLanguageDialect().getChameleonTokenType(dialectIdentifier);
    }
}
