package com.dci.intellij.dbn.language.common.element.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class ParserContext extends ElementLookupContext {
    private long timestamp = System.currentTimeMillis();
    private ParserBuilder builder;
    private Map<Branch, ParsePathNode> branchMarkers = new HashMap<Branch, ParsePathNode>();
    private LeafElementType lastResolvedLeaf;
    private TokenType wavedTokenType;
    private int wavedTokenTypeOffset;

    public ParserContext(PsiBuilder builder, DBLanguageDialect languageDialect, double databaseVersion) {
        super(null, databaseVersion);
        this.builder = new ParserBuilder(builder, languageDialect);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ParserBuilder getBuilder() {
        return builder;
    }

    public void addBranchMarker(ParsePathNode parentNode, Branch branch) {
        branchMarkers.put(branch, parentNode);
        this.branches = branchMarkers.keySet();
    }

    public void removeBranchMarkers(ParsePathNode parentNode) {
        if (branchMarkers.size() > 0 && branchMarkers.containsValue(parentNode)) {
            Iterator<Branch> iterator = branchMarkers.keySet().iterator();
            while (iterator.hasNext()) {
                Branch key = iterator.next();
                if (branchMarkers.get(key) == parentNode) {
                    iterator.remove();
                }
            }
        }
        branches = branchMarkers.size() == 0 ? null : branchMarkers.keySet();
    }

    public LeafElementType getLastResolvedLeaf() {
        return lastResolvedLeaf;
    }

    public void setLastResolvedLeaf(LeafElementType lastResolvedLeaf) {
        this.lastResolvedLeaf = lastResolvedLeaf;
    }

    public boolean isWavedTokenType(TokenType tokenType) {
        return tokenType == wavedTokenType && builder.getCurrentOffset() == wavedTokenTypeOffset;
    }

    public void setWavedTokenType(TokenType wavedTokenType) {
        this.wavedTokenType = wavedTokenType;
        this.wavedTokenTypeOffset = builder.getCurrentOffset();
    }
}
