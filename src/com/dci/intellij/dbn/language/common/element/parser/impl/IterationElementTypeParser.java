package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.BasicElementType;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementType;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class IterationElementTypeParser extends ElementTypeParser<IterationElementType> {
    public IterationElementTypeParser(IterationElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.builder;
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        ElementType iteratedElementType = elementType.getIteratedElementType();
        TokenElementType[] separatorTokens = elementType.getSeparatorTokens();

        int iterations = 0;
        int matchedTokens = 0;
        //TokenType tokenType = (TokenType) builder.getTokenType();
        // check if the token objectType can be part of this iteration
        //if (isDummyToken(builder.getTokenText()) || isSuppressibleReservedWord(tokenType, node) || iteratedElementType.containsToken(tokenType)) {
            ParseResult result = iteratedElementType.getParser().parse(node, optional, depth + 1, context);


            // check first iteration element
            if (result.isMatch()) {
                if (node.isRecursive(node.getStartOffset())) {
                    ParseResultType resultType = matchesMinIterations(iterations) ? ParseResultType.FULL_MATCH : ParseResultType.NO_MATCH;
                    return stepOut(node, context, depth, resultType, matchedTokens);
                }
                while (true) {
                    iterations++;
                    // check separator
                    // if not matched just step out
                    PsiBuilder.Marker partialMatchMarker = null;
                    if (separatorTokens != null) {
                        if (elementType.isFollowedBySeparator()) {
                            partialMatchMarker = builder.mark(null);
                        }
                        for (TokenElementType separatorToken : separatorTokens) {
                            result = separatorToken.getParser().parse(node, false, depth + 1, context);
                            matchedTokens = matchedTokens + result.matchedTokens;
                            if (result.isMatch()) break;
                        }

                        if (result.isNoMatch()) {
                            // if NO_MATCH, no additional separator found, hence then iteration should exit with MATCH
                            ParseResultType resultType =
                                    matchesMinIterations(iterations) ?
                                        matchesIterations(iterations) ?
                                            ParseResultType.FULL_MATCH :
                                            ParseResultType.PARTIAL_MATCH :
                                    ParseResultType.NO_MATCH;

                            builder.markerDrop(partialMatchMarker);
                            return stepOut(node, context, depth, resultType, matchedTokens);
                        } else {
                            node.setCurrentOffset(builder.getCurrentOffset());
                        }
                    }

                    // check consecutive iterated element
                    // if not matched, step out with error

                    result = iteratedElementType.getParser().parse(node, true, depth + 1, context);

                    if (result.isNoMatch()) {
                        // missing separators permit ending the iteration as valid at any time
                        if (separatorTokens == null) {
                            ParseResultType resultType =
                                    matchesMinIterations(iterations) ?
                                        matchesIterations(iterations) ?
                                            ParseResultType.FULL_MATCH :
                                            ParseResultType.PARTIAL_MATCH :
                                    ParseResultType.NO_MATCH;
                            return stepOut(node, context, depth, resultType, matchedTokens);
                        } else {
                            if (matchesMinIterations(iterations)) {
                                if (elementType.isFollowedBySeparator()) {
                                    builder.markerRollbackTo(partialMatchMarker, null);
                                    return stepOut(node, context, depth, ParseResultType.FULL_MATCH, matchedTokens);
                                } else {
                                    builder.markerDrop(partialMatchMarker);
                                }

                                boolean exit = advanceLexerToNextLandmark(node, false, context);
                                if (exit){
                                    return stepOut(node, context, depth, ParseResultType.PARTIAL_MATCH, matchedTokens);
                                }
                            } else {
                                builder.markerDrop(partialMatchMarker);
                                return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
                            }
                        }
                    } else {
                        builder.markerDrop(partialMatchMarker);
                        matchedTokens = matchedTokens + result.matchedTokens;
                    }
                }
            }
        //}
        if (!optional) {
            //updateBuilderError(builder, this);
        }
        return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
    }

    private boolean advanceLexerToNextLandmark(ParsePathNode parentNode, boolean lenient, ParserContext context) {
        ParserBuilder builder = context.builder;

        PsiBuilder.Marker marker = builder.mark(null);
        ElementType iteratedElementType = elementType.getIteratedElementType();
        TokenElementType[] separatorTokens = elementType.getSeparatorTokens();

        if (!lenient) {
            Set<TokenType> expectedTokens = iteratedElementType.getLookupCache().captureFirstPossibleTokens(context.reset());
            ParseBuilderErrorHandler.updateBuilderError(expectedTokens, context);
        }
        boolean advanced = false;
        BasicElementType unknownElementType = getElementBundle().getUnknownElementType();
        while (!builder.eof()) {
            TokenType tokenType = builder.getTokenType();
            if (tokenType == null || tokenType.isChameleon())  break;

            if (tokenType.isParserLandmark()) {
                if (separatorTokens != null) {
                    for (TokenElementType separatorToken : separatorTokens) {
                        if (separatorToken.getLookupCache().containsToken(tokenType)) {
                            builder.markerDone(marker, unknownElementType);
                            return false;
                        }
                    }
                }

                ParsePathNode parseNode = parentNode;
                while (parseNode != null) {
                    if (parseNode.elementType instanceof SequenceElementType) {
                        SequenceElementType sequenceElementType = (SequenceElementType) parseNode.elementType;
                        int index = parseNode.getCursorPosition();
                        if (!iteratedElementType.getLookupCache().containsToken(tokenType) && sequenceElementType.containsLandmarkTokenFromIndex(tokenType, index + 1)) {
                            if (advanced || !lenient) {
                                builder.markerDone(marker, unknownElementType);
                            } else {
                                builder.markerRollbackTo(marker, null);
                            }
                            return true;
                        }

                    }
                    parseNode = parseNode.parent;
                }
            }
            builder.advanceLexer(parentNode);
            advanced = true;
        }
        if (advanced || !lenient)
            builder.markerDone(marker, unknownElementType); else
            builder.markerRollbackTo(marker, null);
        return true;
    }

    private boolean matchesMinIterations(int iterations) {
        Integer minIterations = elementType.getMinIterations();
        return minIterations == null || minIterations <= iterations;
    }

    private boolean matchesIterations(int iterations) {
        int[]elementsCountVariants = elementType.getElementsCountVariants();
        if (elementsCountVariants != null) {
            for (int elementsCountVariant: elementsCountVariants) {
                if (elementsCountVariant == iterations) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
