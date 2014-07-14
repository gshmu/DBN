package com.dci.intellij.dbn.language.common.element.parser;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.util.containers.Stack;

public class NestedRangeMonitor {
    private int stackSize = 0;
    private Stack<NestedRangeStartMarker> rangesStack = new Stack<NestedRangeStartMarker>();
    private DBLanguageDialect languageDialect;
    private PsiBuilder builder;

    private SimpleTokenType leftParenthesis;
    private SimpleTokenType rightParenthesis;


    public NestedRangeMonitor(PsiBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;
        this.languageDialect = languageDialect;

        SharedTokenTypeBundle sharedTokenTypes = languageDialect.getParserTokenTypes().getSharedTokenTypes();
        leftParenthesis = sharedTokenTypes.getLeftParenthesis();
        rightParenthesis = sharedTokenTypes.getRightParenthesis();
    }

    /**
     * cleanup all markers registered after the builder offset (remained dirty after a marker rollback)
     */
    public void rollback() {
        int builderOffset = builder.getCurrentOffset();
        while (rangesStack.size() > 0) {
            NestedRangeStartMarker lastMarker = rangesStack.peek();
            if (lastMarker.getOffset() >= builderOffset) {
                rangesStack.pop();
                lastMarker.dropMarker();
                if (stackSize > 0) stackSize--;
            } else {
                break;
            }
        }
    }

    public void compute(@NotNull ParsePathNode node, boolean mark) {
        TokenType tokenType = (TokenType) builder.getTokenType();
        if (tokenType == leftParenthesis) {
            stackSize++;
            NestedRangeStartMarker marker = new NestedRangeStartMarker(node, builder, mark);
            rangesStack.push(marker);
        } else if (tokenType == rightParenthesis) {
            if (stackSize > 0) stackSize--;
            if (rangesStack.size() > 0) {
                NestedRangeStartMarker marker = rangesStack.peek();
                ParsePathNode markerNode = marker.getParentNode();
                if (markerNode == node) {

                } else if (markerNode.isSiblingOf(node)) {

                } else if (node.isSiblingOf(markerNode)) {
                    WrappingDefinition childWrapping = node.getElementType().getWrapping();
                    WrappingDefinition parentWrapping = markerNode.getElementType().getWrapping();
                    if (childWrapping != null && childWrapping.equals(parentWrapping)) {

                    }
                }
                cleanup(false);
            }
        }
    }

    public void cleanup(boolean force) {
        if (force) stackSize = 0;
        while(rangesStack.size() > stackSize) {
            NestedRangeStartMarker lastMarker = rangesStack.pop();
            lastMarker.dropMarker();
        }
    }
}
