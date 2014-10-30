package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.BlockElementType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeLogger;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeUtil;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder;

public abstract class AbstractElementTypeParser<T extends ElementType> implements ElementTypeParser<T>{
    private T elementType;
    private ElementTypeLogger logger;

    public AbstractElementTypeParser(T elementType) {
        this.elementType = elementType;
    }

    protected boolean isDummyToken(String tokenText){
        return tokenText != null && tokenText.contains(CodeCompletionContributor.DUMMY_TOKEN);
    }

    public T getElementType() {
        return elementType;
    }

    private ElementTypeLogger getLogger() {
        if (logger == null) logger = new ElementTypeLogger(getElementType());
        return logger;
    }

    public void logBegin(ParserBuilder builder, boolean optional, int depth) {
        if (SettingsUtil.isDebugEnabled) {
            getLogger().logBegin(builder, optional, depth);
        }
    }

    public void logEnd(ParseResultType resultType, int depth) {
        if (SettingsUtil.isDebugEnabled) {
            getLogger().logEnd(resultType, depth);
        }
    }
    public ParsePathNode stepIn(ParsePathNode parentParseNode, ParserContext context) {
        ParserBuilder builder = context.getBuilder();
        ParsePathNode node = new ParsePathNode(elementType, parentParseNode, builder.getCurrentOffset(), 0);
        PsiBuilder.Marker marker = builder.mark(node);
        node.setElementMarker(marker);
        return node;
    }

    protected ParseResult stepOut(ParsePathNode node, ParserContext context, int depth, ParseResultType resultType, int matchedTokens) {
        return stepOut(null, node, context, depth, resultType, matchedTokens);
    }

    protected ParseResult stepOut(PsiBuilder.Marker marker, ParsePathNode node, ParserContext context, int depth, ParseResultType resultType, int matchedTokens) {
        try {
            marker = marker == null ? node == null ? null : node.getElementMarker() : marker;
            if (resultType == ParseResultType.PARTIAL_MATCH) {
                ParseBuilderErrorHandler.updateBuilderError(elementType.getLookupCache().getNextPossibleTokens(), context);
            }
            ParserBuilder builder = context.getBuilder();
            T elementType = getElementType();
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
                    context.addBranchMarker(node.getParent(), branch);
                }
                if (elementType instanceof LeafElementType) {
                    LeafElementType leafElementType = (LeafElementType) elementType;
                    context.setLastResolvedLeaf(leafElementType);
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
        if (tokenType != null) {
            if (tokenType.isSuppressibleReservedWord()) {
                SharedTokenTypeBundle sharedTokenTypes = getElementBundle().getTokenTypeBundle().getSharedTokenTypes();
                SimpleTokenType dot = sharedTokenTypes.getChrDot();
                SimpleTokenType leftParenthesis = sharedTokenTypes.getChrLeftParenthesis();
                ParserBuilder builder = context.getBuilder();
                if (builder.lookBack(1) == dot || builder.lookAhead(1) == dot) {
                    return true;
                }
                if (tokenType.isFunction() && builder.lookAhead(1) != leftParenthesis) {
                    return true;
                }

                ElementType namedElementType = ElementTypeUtil.getEnclosingNamedElementType(node);
                if (namedElementType != null && namedElementType.getLookupCache().containsToken(tokenType)) {
                    LeafElementType lastResolvedLeaf = context.getLastResolvedLeaf();
                    if (lastResolvedLeaf == null || lastResolvedLeaf.isNextPossibleToken(tokenType, node)) {
                        return false;
                    }

                    return true;
                }



                return true;//!isFollowedByToken(tokenType, node);
            }
        }
        return false;
    }

    private boolean isFollowedByToken(TokenType tokenType, ParsePathNode node) {
        ParsePathNode parent = node;
        while (parent != null) {
            if (parent.getElementType() instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) parent.getElementType();
                if (sequenceElementType.isPossibleTokenFromIndex(tokenType, parent.getCursorPosition() + 1)) {
                    return true;
                }
            }
            // break when statement boundary found
            /*if (parent.getElementType().is(ElementTypeAttribute.STATEMENT)) {
                return false;
            }*/
            parent = parent.getParent();
        }
        return false;
    }

    public ElementTypeBundle getElementBundle() {
        return elementType.getElementBundle();
    }
}
