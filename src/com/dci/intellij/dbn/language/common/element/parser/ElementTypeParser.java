package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.impl.BlockElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeLogger;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeUtil;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.isLanguageParserDebug;

public abstract class ElementTypeParser<T extends ElementTypeBase> {
    public final T elementType;

    public ElementTypeParser(T elementType) {
        this.elementType = elementType;
    }

    protected boolean isDummyToken(String tokenText){
        return tokenText != null && tokenText.contains(CodeCompletionContributor.DUMMY_TOKEN);
    }

    public void logBegin(ParserBuilder builder, boolean optional, int depth) {
        if (isLanguageParserDebug()) {
            ElementTypeLogger.logBegin(elementType, builder, optional, depth);
        }
    }

    public void logEnd(ParseResultType resultType, int depth) {
        if (isLanguageParserDebug()) {
            ElementTypeLogger.logEnd(elementType, resultType, depth);
        }
    }
    public ParsePathNode stepIn(ParsePathNode parentParseNode, ParserContext context) {
        ParserBuilder builder = context.builder;
        ParsePathNode node = new ParsePathNode(elementType, parentParseNode, builder.getCurrentOffset(), 0);
        PsiBuilder.Marker marker = builder.mark(node);
        node.setElementMarker(marker);
        return node;
    }

    public ParseResult stepOut(ParsePathNode node, ParserContext context, int depth, ParseResultType resultType, int matchedTokens) {
        return stepOut(null, node, context, depth, resultType, matchedTokens);
    }

    public ParseResult stepOut(PsiBuilder.Marker marker, ParsePathNode node, ParserContext context, int depth, ParseResultType resultType, int matchedTokens) {
        try {
            marker = marker == null ? node == null ? null : node.getElementMarker() : marker;
            if (resultType == ParseResultType.PARTIAL_MATCH) {
                ElementTypeBase offsetPsiElement = CommonUtil.nvl(context.lastResolvedLeaf, elementType);
                Set<TokenType> nextPossibleTokens = offsetPsiElement.getLookupCache().getNextPossibleTokens();
                ParseBuilderErrorHandler.updateBuilderError(nextPossibleTokens, context);
            }
            ParserBuilder builder = context.builder;
            if (resultType == ParseResultType.NO_MATCH) {
                builder.markerRollbackTo(marker, node);
            } else {
                if (elementType instanceof BlockElementType)
                    builder.markerDrop(marker); else
                    builder.markerDone(marker, elementType, node);
            }


            logEnd(resultType, depth);
            if (resultType == ParseResultType.NO_MATCH) {
                return ParseResult.createNoMatchResult();
            } else {
                Branch branch = this.elementType.getBranch();
                if (node != null && branch != null) {
                    // if node is matched add branches marker
                    context.addBranchMarker(node, branch);
                }
                if (elementType instanceof LeafElementType) {
                    context.lastResolvedLeaf = (LeafElementType) elementType;
                }

                return ParseResult.createFullMatchResult(matchedTokens);
            }
        } finally {
            if (node != null) {
                context.removeBranchMarkers(node);
                node.detach();

            }

        }
    }

    /**
     * Returns true if the token is a reserved word, but can act as an identifier in this context.
     */
    protected boolean isSuppressibleReservedWord(TokenType tokenType, ParsePathNode node, ParserContext context) {
        if (tokenType != null && tokenType.isSuppressibleReservedWord()) {
            SharedTokenTypeBundle sharedTokenTypes = getElementBundle().getTokenTypeBundle().getSharedTokenTypes();
            SimpleTokenType dot = sharedTokenTypes.getChrDot();
            SimpleTokenType leftParenthesis = sharedTokenTypes.getChrLeftParenthesis();
            ParserBuilder builder = context.builder;
            if (builder.lookBack(1) == dot || builder.lookAhead(1) == dot) {
                return true;
            }

            if (tokenType.isFunction() && builder.lookAhead(1) != leftParenthesis) {
                if (elementType instanceof LeafElementType) {
                    LeafElementType leafElementType = (LeafElementType) elementType;
                    return !leafElementType.isNextRequiredToken(leftParenthesis, node, context);
                }
            }

            ElementTypeBase namedElementType = ElementTypeUtil.getEnclosingNamedElementType(node);
            if (namedElementType != null && namedElementType.getLookupCache().containsToken(tokenType)) {
                LeafElementType lastResolvedLeaf = context.lastResolvedLeaf;
                return lastResolvedLeaf != null && !lastResolvedLeaf.isNextPossibleToken(tokenType, node, context);
            }

            if (context.lastResolvedLeaf != null) {
                if (context.lastResolvedLeaf.isNextPossibleToken(tokenType, node, context)) {
                    return false;
                }
            }
            return true;//!isFollowedByToken(tokenType, node);
        }
        return false;
    }

    public ElementTypeBundle getElementBundle() {
        return elementType.getElementBundle();
    }

    protected boolean shouldParseElement(ElementTypeBase elementType, ParsePathNode node, ParserContext context) {
        ParserBuilder builder = context.builder;
        TokenType tokenType = builder.getTokenType();

        return
            elementType.getLookupCache().couldStartWithToken(tokenType) ||
            isSuppressibleReservedWord(tokenType, node, context) ||
            isDummyToken(builder.getTokenText());
    }

    @Override
    public String toString() {
        return elementType.toString();
    }

    protected SharedTokenTypeBundle getSharedTokenTypes() {
        return elementType.getLanguage().getSharedTokenTypes();
    }

    public abstract ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException;
}
