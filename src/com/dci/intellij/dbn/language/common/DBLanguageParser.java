package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import lombok.Getter;
import org.jdom.Document;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class DBLanguageParser implements PsiParser {
    private final DBLanguageDialect languageDialect;
    private final ElementTypeBundle elementTypes;
    private final TokenTypeBundle tokenTypes;
    private final String defaultParseRootId;

    public DBLanguageParser(DBLanguageDialect languageDialect, String tokenTypesFile, String elementTypesFile, String defaultParseRootId) {
        this.languageDialect = languageDialect;
        this.tokenTypes = new TokenTypeBundle(languageDialect, Commons.loadXmlFile(getResourceLookupClass(), tokenTypesFile));
        Document document = Commons.loadXmlFile(getResourceLookupClass(), elementTypesFile);
        this.elementTypes = new ElementTypeBundle(languageDialect, tokenTypes, document);
        this.defaultParseRootId = defaultParseRootId;
    }

    protected Class getResourceLookupClass() {
        return getClass();
    }

    @Override
    @NotNull
    public ASTNode parse(@NotNull IElementType rootElementType, @NotNull PsiBuilder builder) {
        return parse(rootElementType, builder, defaultParseRootId, 9999);
    }

    @NotNull
    public ASTNode parse(IElementType rootElementType, PsiBuilder psiBuilder, String parseRootId, double databaseVersion) {
        ParserContext context = new ParserContext(psiBuilder, languageDialect, databaseVersion);
        ParserBuilder builder = context.getBuilder();
        if (parseRootId == null ) parseRootId = defaultParseRootId;
        PsiBuilder.Marker marker = builder.mark();
        NamedElementType root =  elementTypes.getNamedElementType(parseRootId);
        if (root == null) {
            root = elementTypes.getRootElementType();
        }

        boolean advancedLexer = false;
        ParsePathNode rootParseNode = new ParsePathNode(root, null, 0, 0);

        try {
            while (!builder.eof()) {
                int currentOffset =  builder.getCurrentOffset();
                root.getParser().parse(rootParseNode, context);
                if (currentOffset == builder.getCurrentOffset()) {
                    TokenType token = builder.getTokenType();
                    /*if (tokenType.isChameleon()) {
                        PsiBuilder.Marker injectedLanguageMarker = builder.mark();
                        builder.advanceLexer();
                        injectedLanguageMarker.done((IElementType) tokenType);
                    }
                    else*/ if (token instanceof ChameleonTokenType) {
                        PsiBuilder.Marker injectedLanguageMarker = builder.mark();
                        builder.advanceLexer(rootParseNode);
                        injectedLanguageMarker.done((IElementType) token);
                    } else {
                        builder.advanceLexer(rootParseNode);
                    }
                    advancedLexer = true;
                }
            }
        } catch (ParseException e) {
            while (!builder.eof()) {
                builder.advanceLexer(rootParseNode);
                advancedLexer = true;
            }
        } catch (StackOverflowError e) {
            builder.markerRollbackTo(marker);
            marker = builder.mark();
            while (!builder.eof()) {
                builder.advanceLexer(rootParseNode);
                advancedLexer = true;
            }

        }

        if (!advancedLexer) builder.advanceLexer(rootParseNode);
        marker.done(rootElementType);
        return builder.getTreeBuilt();
    }
}
