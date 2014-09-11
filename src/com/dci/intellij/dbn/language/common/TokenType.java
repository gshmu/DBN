package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;

public interface TokenType {
    String getId();

    int getIdx();

    String getValue();

    String getDescription();

    String getTypeName();

    boolean isSuppressibleReservedWord();

    boolean isIdentifier();

    boolean isVariable();

    boolean isQuotedIdentifier();

    boolean isKeyword();

    boolean isFunction();

    boolean isParameter();

    boolean isDataType();

    boolean isCharacter();

    boolean isOperator();

    boolean isChameleon();

    boolean isReservedWord();

    boolean isParserLandmark();

    TokenTypeCategory getCategory();

    FormattingDefinition getFormatting();

    TokenPairTemplate getTokenPairTemplate();

    void setDefaultFormatting(FormattingDefinition defaults);

    boolean isOneOf(TokenType ... tokenTypes);

    boolean matches(TokenType tokenType);
}
