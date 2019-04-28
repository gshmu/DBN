package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.common.index.Indexable;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public interface TokenType extends Indexable {
    AtomicInteger INDEXER = new AtomicInteger();

    String getId();

    int getLookupIndex();

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

    boolean isLiteral();

    boolean isNumeric();

    boolean isCharacter();

    boolean isOperator();

    boolean isChameleon();

    boolean isReservedWord();

    boolean isParserLandmark();

    @NotNull
    TokenTypeCategory getCategory();

    @Nullable
    DBObjectType getObjectType();

    FormattingDefinition getFormatting();

    TokenPairTemplate getTokenPairTemplate();

    void setDefaultFormatting(FormattingDefinition defaults);

    boolean isOneOf(TokenType ... tokenTypes);

    boolean matches(TokenType tokenType);
}
