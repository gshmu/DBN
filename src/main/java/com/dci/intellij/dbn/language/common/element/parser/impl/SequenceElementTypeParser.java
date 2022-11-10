package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementType;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.LanguageNode;
import com.dci.intellij.dbn.language.common.element.path.ParserNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder;

import java.util.Set;

import static com.dci.intellij.dbn.language.common.element.parser.ParseResultType.*;

public class SequenceElementTypeParser<ET extends SequenceElementType> extends ElementTypeParser<ET> {
    public SequenceElementTypeParser(ET elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParserNode parentNode, ParserContext context) throws ParseException {
        if (context.isAlternative()) return parseNew(parentNode, context);

        ParserBuilder builder = context.getBuilder();
        ParserNode node = stepIn(parentNode, context);

        int matches = 0;
        int matchedTokens = 0;

        TokenType token = builder.getToken();

        if (token != null && !token.isChameleon() && shouldParseElement(elementType, node, context)) {
            ElementTypeRef[] elements = elementType.getChildren();
            while (node.getCursorPosition() < elements.length) {
                int index = node.getCursorPosition();
                ElementTypeRef element = elements[index];

                // end of document / language switch
                if (token == null || token.isChameleon()) {

                    if (element.isFirst() || elementType.isExitIndex(index)) {
                        return stepOut(node, context, NO_MATCH, matchedTokens);
                    }

                    if (element.isOptional() && element.isOptionalFromHere()) {
                        return stepOut(node, context, FULL_MATCH, matchedTokens);
                    }

                    return stepOut(node, context, PARTIAL_MATCH, matchedTokens);
                }

                if (context.check(element)) {
                    ParseResult result = ParseResult.noMatch();
                    if (shouldParseElement(element.getElementType(), node, context)) {

                        //node = node.createVariant(builder.getCurrentOffset(), i);
                        result = element.getParser().parse(node, context);

                        if (result.isMatch()) {
                            matchedTokens = matchedTokens + result.getMatchedTokens();
                            token = builder.getToken();
                            matches++;
                        }
                    }

                    // not matched and not optional
                    if (result.isNoMatch() && !element.isOptional()) {
                        boolean isWeakMatch = matches < 2 && matchedTokens < 3 && index > 1 && ignoreFirstMatch();

                        if (element.isFirst() || elementType.isExitIndex(index) || isWeakMatch || matches == 0) {
                            //if (isFirst(i) || isExitIndex(i)) {
                            return stepOut(node, context, NO_MATCH, matchedTokens);
                        }

                        index = advanceLexerToNextLandmark(node, context);

                        if (index <= 0) {
                            // no landmarks found or landmark in parent found
                            return stepOut(node, context, PARTIAL_MATCH, matchedTokens);
                        } else {
                            // local landmarks found

                            token = builder.getToken();
                            node.setCursorPosition(index);
                            continue;
                        }
                    }
                }


                // if is last element
                if (element.isLast()) {
                    //matches == 0 reaches this stage only if all sequence elements are optional
                    ParseResultType resultType = matches == 0 ? NO_MATCH : FULL_MATCH;
                    return stepOut(node, context, resultType, matchedTokens);
                }
                node.incrementIndex(builder.getOffset());
            }
        }

        return stepOut(node, context, NO_MATCH, matchedTokens);
    }

    ParseResult parseNew(ParserNode parentNode, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        ParserNode node = stepIn(parentNode, context);

        ElementTypeRef element = elementType.getFirstChild();
        while (element != null) {


            element = element.getNext();
        }


        return ParseResult.noMatch(); // TODO
    }

    @Deprecated // ambiguous
    private boolean ignoreFirstMatch() {
        ElementTypeRef firstChild = elementType.getChild(0);
        ElementType elementType = firstChild.getElementType();
        if (elementType instanceof IdentifierElementType) {
            IdentifierElementType identifierElementType = (IdentifierElementType) elementType;
            return !identifierElementType.isDefinition();
        }
        return false;
    }

    private int advanceLexerToNextLandmark(ParserNode node, ParserContext context) {
        int siblingPosition = node.getCursorPosition();
        ParserBuilder builder = context.getBuilder();
        PsiBuilder.Marker marker = builder.mark();
        Set<TokenType> possibleTokens = elementType.getFirstPossibleTokensFromIndex(context, siblingPosition);
        ParseBuilderErrorHandler.updateBuilderError(possibleTokens, context);

        TokenType tokenType = builder.getToken();
        siblingPosition++;
        while (tokenType != null) {
            int newIndex = getLandmarkIndex(tokenType, siblingPosition, node);

            // no landmark hit -> spool the builder
            if (newIndex == 0) {
                builder.advance();
                tokenType = builder.getToken();
            } else {
                //builder.markerDone(marker, getElementBundle().getUnknownElementType());
                marker.error("Invalid or incomplete statement. Expected: ");
                return newIndex;
            }
        }
        //builder.markerDone(marker, getElementBundle().getUnknownElementType());
        marker.error("Invalid or incomplete statement. Expected: ");
        return 0;
    }

    private int getLandmarkIndex(TokenType tokenType, int index, ParserNode node) {
        if (tokenType.isParserLandmark()) {
            LanguageNode statementPathNode = node.getParent(ElementTypeAttribute.STATEMENT);
            if (statementPathNode != null && statementPathNode.getElement().getLookupCache().couldStartWithToken(tokenType)) {
                return -1;
            }
            ElementTypeRef[] children = elementType.getChildren();
            for (int i=index; i< children.length; i++) {
                // check children landmarks
                if (children[i].getLookupCache().couldStartWithToken(tokenType)) {
                    return i;
                }
            }

            ParserNode parseNode = node;
            while (parseNode != null) {
                ElementType elementType = parseNode.getElement();
                if (elementType instanceof SequenceElementType) {
                    SequenceElementType sequenceElementType = (SequenceElementType) elementType;
                    if ( sequenceElementType.containsLandmarkTokenFromIndex(tokenType, parseNode.getCursorPosition() + 1)) {
                        return -1;
                    }
                } else  if (elementType instanceof IterationElementType) {
                    IterationElementType iterationElementType = (IterationElementType) elementType;
                    if (iterationElementType.isSeparator(tokenType)) {
                        return -1;
                    }
                }
                parseNode = parseNode.getParent();
            }
        }
        return 0;
    }
}
