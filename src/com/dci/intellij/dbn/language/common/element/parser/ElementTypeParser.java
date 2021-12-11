package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.impl.BlockElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeUtil;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder.Marker;

import java.util.Set;

public abstract class ElementTypeParser<T extends ElementTypeBase> {
    public final T elementType;

    public ElementTypeParser(T elementType) {
        this.elementType = elementType;
    }

    protected boolean isDummyToken(String tokenText){
        return tokenText != null && tokenText.contains(CodeCompletionContributor.DUMMY_TOKEN);
    }

    public ParsePathNode stepIn(ParsePathNode parentNode, ParserContext context) {
        ParserBuilder builder = context.getBuilder();
        ParsePathNode node = new ParsePathNode(elementType, parentNode, builder.getCurrentOffset(), 0);
        Marker marker = builder.mark(node);
        node.setElementMarker(marker);
        return node;
    }

    public ParseResult stepOut(ParsePathNode node, ParserContext context, ParseResultType resultType, int matchedTokens) {
        return stepOut(null, node, context, resultType, matchedTokens);
    }

    public ParseResult stepOut(Marker marker, ParsePathNode node, ParserContext context, ParseResultType resultType, int matchedTokens) {
        try {
            marker = marker == null ? node == null ? null : node.getElementMarker() : marker;
            if (resultType == ParseResultType.PARTIAL_MATCH) {
                ElementTypeBase offsetPsiElement = Commons.nvl(context.getLastResolvedLeaf(), elementType);
                Set<TokenType> nextPossibleTokens = offsetPsiElement.getLookupCache().getNextPossibleTokens();
                ParseBuilderErrorHandler.updateBuilderError(nextPossibleTokens, context);
            }
            ParserBuilder builder = context.getBuilder();
            if (resultType == ParseResultType.NO_MATCH) {
                builder.markerRollbackTo(marker);
            } else {
                if (elementType instanceof BlockElementType)
                    builder.markerDrop(marker); else
                    builder.markerDone(marker, elementType, node);
            }


            if (resultType == ParseResultType.NO_MATCH) {
                return ParseResult.noMatch();
            } else {
                Branch branch = this.elementType.getBranch();
                if (node != null && branch != null) {
                    // if node is matched add branches marker
                    context.addBranchMarker(node, branch);
                }
                if (elementType instanceof LeafElementType) {
                    context.setLastResolvedLeaf((LeafElementType) elementType);
                }

                return ParseResult.fullMatch(matchedTokens);
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
            ParserBuilder builder = context.getBuilder();
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
                LeafElementType lastResolvedLeaf = context.getLastResolvedLeaf();
                return lastResolvedLeaf != null && !lastResolvedLeaf.isNextPossibleToken(tokenType, node, context);
            }

            if (context.getLastResolvedLeaf() != null) {
                if (context.getLastResolvedLeaf().isNextPossibleToken(tokenType, node, context)) {
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
        ParserBuilder builder = context.getBuilder();
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

    public abstract ParseResult parse(ParsePathNode parentNode, ParserContext context) throws ParseException;
}
