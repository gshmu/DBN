package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinitionFactory;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleTokenType extends IElementType implements TokenType {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private int idx;
    private String id;
    private String value;
    private String description;
    private boolean isSuppressibleReservedWord;
    private TokenTypeCategory category;
    private DBObjectType objectType;
    private int lookupIndex;
    private int hashCode;
    private FormattingDefinition formatting;
    private TokenPairTemplate tokenPairTemplate;
    private static final AtomicInteger REGISTERED_COUNT = new AtomicInteger();

    public SimpleTokenType(@NotNull @NonNls String debugName, @Nullable Language language) {
        super(debugName, language, false);
    }

/*    public SimpleTokenType(SimpleTokenType source, Language language) {
        super(source.toString(), language);
        idx = INDEXER.incrementAndGet();
        this.id = source.id;
        this.value = source.getValue();
        this.description = source.description;
        isSuppressibleReservedWord = source.isSuppressibleReservedWord();
        this.category = source.category;
        this.objectType = source.objectType;
        this.lookupIndex = source.lookupIndex;

        formatting = FormattingDefinitionFactory.cloneDefinition(source.getFormatting());
        tokenPairTemplate = TokenPairTemplate.get(id);
    }*/

    public SimpleTokenType(Element element, Language language, boolean register) {
        super(element.getAttributeValue("id").intern(), language, register);
        idx = INDEXER.incrementAndGet();
        id = element.getAttributeValue("id").intern();
        value = StringUtil.intern(element.getAttributeValue("value"));
        description = StringUtil.intern(element.getAttributeValue("description"));

        if (register) {
            int count = REGISTERED_COUNT.incrementAndGet();
            LOGGER.info("Registering element " + id + " for language " + language.getID() + " (" + count + ")");
        }

        String indexString = element.getAttributeValue("index");
        if (StringUtil.isNotEmptyOrSpaces(indexString)) {
            lookupIndex = Integer.parseInt(indexString);
        }

        String type = element.getAttributeValue("type");
        category = TokenTypeCategory.getCategory(type);
        isSuppressibleReservedWord = isReservedWord() && !Boolean.parseBoolean(element.getAttributeValue("reserved"));
        hashCode = (language.getDisplayName() + id).hashCode();

        String objectType = element.getAttributeValue("objectType");
        if (StringUtil.isNotEmpty(objectType)) {
            this.objectType = DBObjectType.get(objectType);
        }

        formatting = FormattingDefinitionFactory.loadDefinition(element);
        tokenPairTemplate = TokenPairTemplate.get(id);
    }

    @Override
    public int getIdx() {
        return idx;
    }

    @Override
    public TokenPairTemplate getTokenPairTemplate() {
        return tokenPairTemplate;
    }

    @Override
    public void setDefaultFormatting(FormattingDefinition defaultFormatting) {
        formatting = FormattingDefinitionFactory.mergeDefinitions(formatting, defaultFormatting);
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }


    @Override
    public int getLookupIndex() {
        return lookupIndex;
    }

    @Override
    public String getValue() {
        return value == null ? "" : value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTypeName() {
        return category.getName();
    }

    public int compareTo(Object o) {
        SimpleTokenType tokenType = (SimpleTokenType) o;
        return getValue().compareTo(tokenType.getValue());
    }

    @Override
    public boolean isSuppressibleReservedWord() {
        return isReservedWord() && isSuppressibleReservedWord;
    }

    @Override
    public boolean isIdentifier() {
        return category == TokenTypeCategory.IDENTIFIER;
    }

    @Override
    public boolean isVariable() {
        return getSharedTokenTypes().isVariable(this);
    }

    @Override
    public boolean isQuotedIdentifier() {
        return this == getSharedTokenTypes().getQuotedIdentifier();
    }

    @Override
    public boolean isKeyword() {
        return category == TokenTypeCategory.KEYWORD;
    }

    @Override
    public boolean isFunction() {
        return category == TokenTypeCategory.FUNCTION;
    }

    @Override
    public boolean isParameter() {
        return category == TokenTypeCategory.PARAMETER;
    }

    @Override
    public boolean isDataType() {
        return category == TokenTypeCategory.DATATYPE;
    }

    @Override
    public boolean isLiteral() {
        return category == TokenTypeCategory.LITERAL;
    }

    @Override
    public boolean isNumeric() {
        return category == TokenTypeCategory.NUMERIC;
    }

    @Override
    public boolean isCharacter() {
        return category == TokenTypeCategory.CHARACTER;
    }

    @Override
    public boolean isOperator() {
        return category == TokenTypeCategory.OPERATOR;
    }

    @Override
    public boolean isChameleon() {
        return category == TokenTypeCategory.CHAMELEON;
    }

    @Override
    public boolean isReservedWord() {
        return isKeyword() || isFunction() || isParameter() || isDataType();
    }

    @Override
    public boolean isParserLandmark() {
        return !isIdentifier();
        //return isKeyword() || isFunction() || isParameter() || isCharacter() || isOperator();
        //return isCharacter() || isOperator() || !isSuppressibleReservedWord();
    }

    @Override
    @NotNull
    public TokenTypeCategory getCategory() {
        return category;
    }

    @Nullable
    @Override
    public DBObjectType getObjectType() {
        return objectType;
    }

    @Override
    public FormattingDefinition getFormatting() {
        if (formatting == null) {
            formatting = new FormattingDefinition();
        }
        return formatting;
    }

    @NotNull
    private SharedTokenTypeBundle getSharedTokenTypes() {
        Language lang = getLanguage();
        if (lang instanceof DBLanguageDialect) {
            DBLanguageDialect languageDialect = (DBLanguageDialect) lang;
            return languageDialect.getSharedTokenTypes();
        } else if (lang instanceof DBLanguage) {
            DBLanguage language = (DBLanguage) lang;
            return language.getSharedTokenTypes();
        }
        throw new IllegalArgumentException("Language element of type " + lang + "is not supported");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof SimpleTokenType) {
            SimpleTokenType simpleTokenType = (SimpleTokenType) obj;
            return simpleTokenType.getLanguage().equals(getLanguage()) &&
                    simpleTokenType.id.equals(id);
        }
        return false;
    }

    @Override
    public boolean matches(TokenType tokenType) {
        if (this.equals(tokenType)) return true;
        if (this.isIdentifier() && tokenType.isIdentifier()) return true;
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean isOneOf(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (this == tokenType) return true;
        }
        return false;
    }
}
