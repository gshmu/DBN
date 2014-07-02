package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.psi.tree.TokenSet;

public class SharedTokenTypeBundle extends DBLanguageTokenTypeBundle {
    private SimpleTokenType whiteSpace;
    private SimpleTokenType identifier;
    private SimpleTokenType quotedIdentifier;
    private SimpleTokenType variable;
    private SimpleTokenType string;
    private SimpleTokenType number;
    private SimpleTokenType integer;
    private SimpleTokenType lineComment;
    private SimpleTokenType blockComment;
    private SimpleTokenType leftParenthesis;
    private SimpleTokenType rightParenthesis;

    private SimpleTokenType dot;

    private TokenSet whitespaceTokens;
    private TokenSet commentTokens;
    private TokenSet stringTokens;

    public SharedTokenTypeBundle(DBLanguage language) {
        super(language, CommonUtil.loadXmlFile(SharedTokenTypeBundle.class, "db_language_common_tokens.xml"));
        whiteSpace = getTokenType("WHITE_SPACE");
        identifier = getTokenType("IDENTIFIER");
        quotedIdentifier = getTokenType("QUOTED_IDENTIFIER");
        variable = getTokenType("VARIABLE");
        string = getTokenType("STRING");
        number = getTokenType("NUMBER");
        integer = getTokenType("INTEGER");
        lineComment = getTokenType("LINE_COMMENT");
        blockComment = getTokenType("BLOCK_COMMENT");


        leftParenthesis = getTokenType("CHR_LEFT_PARENTHESIS");
        rightParenthesis = getTokenType("CHR_RIGHT_PARENTHESIS");
        dot = getTokenType("CHR_DOT");

        whitespaceTokens = getTokenSet("WHITE_SPACES");
        commentTokens = getTokenSet("COMMENTS");
        stringTokens = getTokenSet("STRINGS");

    }

    public TokenSet getWhitespaceTokens() {
        return whitespaceTokens;
    }

    public TokenSet getCommentTokens() {
        return commentTokens;
    }

    public SimpleTokenType getWhiteSpace() {
        return whiteSpace;
    }

    public SimpleTokenType getIdentifier() {
        return identifier;
    }

    public SimpleTokenType getQuotedIdentifier() {
        return quotedIdentifier;
    }

    public SimpleTokenType getVariable() {
        return variable;
    }

    public SimpleTokenType getString() {
        return string;
    }

    public SimpleTokenType getNumber() {
        return number;
    }

    public SimpleTokenType getInteger() {
        return integer;
    }

    public SimpleTokenType getLineComment() {
        return lineComment;
    }

    public SimpleTokenType getBlockComment() {
        return blockComment;
    }

    public boolean isIdentifier(TokenType tokenType) {
        return tokenType == identifier || tokenType == quotedIdentifier;
    }

    public boolean isVariable(TokenType tokenType) {
        return tokenType == variable;
    }

    public SimpleTokenType getLeftParenthesis() {
        return leftParenthesis;
    }

    public SimpleTokenType getRightParenthesis() {
        return rightParenthesis;
    }

    public SimpleTokenType getDot() {
        return dot;
    }

    public TokenSet getStringTokens() {
        return stringTokens;
    }
}
