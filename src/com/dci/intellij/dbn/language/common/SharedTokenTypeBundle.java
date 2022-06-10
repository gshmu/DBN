package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.util.XmlContents;
import com.intellij.psi.tree.TokenSet;
import gnu.trove.THashSet;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jdom.Element;

import java.util.Set;

@Getter
public class SharedTokenTypeBundle extends TokenTypeBundleBase {
    private final SimpleTokenType whiteSpace;
    private final SimpleTokenType identifier;
    private final SimpleTokenType quotedIdentifier;
    private final SimpleTokenType variable;
    private final SimpleTokenType string;
    private final SimpleTokenType number;
    private final SimpleTokenType integer;
    private final SimpleTokenType lineComment;
    private final SimpleTokenType blockComment;

    private final SimpleTokenType chrLeftParenthesis;
    private final SimpleTokenType chrRightParenthesis;
    private final SimpleTokenType chrLeftBracket;
    private final SimpleTokenType chrRightBracket;

    private final SimpleTokenType chrDot;
    private final SimpleTokenType chrComma;
    private final SimpleTokenType chrStar;

    private final TokenSet whitespaceTokens;
    private final TokenSet commentTokens;
    private final TokenSet stringTokens;

    private final Set<TokenType> identifierTokens;

    public SharedTokenTypeBundle(DBLanguage language) {
        super(language, loadDefinition());
        whiteSpace = getTokenType("WHITE_SPACE");
        identifier = getTokenType("IDENTIFIER");
        quotedIdentifier = getTokenType("QUOTED_IDENTIFIER");
        variable = getTokenType("VARIABLE");
        string = getTokenType("STRING");
        number = getTokenType("NUMBER");
        integer = getTokenType("INTEGER");
        lineComment = getTokenType("LINE_COMMENT");
        blockComment = getTokenType("BLOCK_COMMENT");


        chrLeftParenthesis = getTokenType("CHR_LEFT_PARENTHESIS");
        chrRightParenthesis = getTokenType("CHR_RIGHT_PARENTHESIS");
        chrLeftBracket = getTokenType("CHR_LEFT_BRACKET");
        chrRightBracket = getTokenType("CHR_RIGHT_BRACKET");

        chrDot = getTokenType("CHR_DOT");
        chrComma = getTokenType("CHR_COMMA");
        chrStar = getTokenType("CHR_STAR");

        whitespaceTokens = getTokenSet("WHITE_SPACES");
        commentTokens = getTokenSet("COMMENTS");
        stringTokens = getTokenSet("STRINGS");

        identifierTokens = new THashSet<>(2);
        identifierTokens.add(identifier);
        identifierTokens.add(quotedIdentifier);
    }

    @SneakyThrows
    private static Element loadDefinition() {
        return XmlContents.loadXmlContent(SharedTokenTypeBundle.class, "db_language_common_tokens.xml");
    }


    public boolean isIdentifier(TokenType tokenType) {
        return tokenType == identifier || tokenType == quotedIdentifier;
    }

    public boolean isVariable(TokenType tokenType) {
        return tokenType == variable;
    }

    public Set<TokenType> getIdentifierTokens() {
        return identifierTokens;
    }
}
