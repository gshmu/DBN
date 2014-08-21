package com.dci.intellij.dbn.language.common.element.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class ParserContext implements ElementLookupContext {
    private long timestamp = System.currentTimeMillis();
    private ParserBuilder builder;
    private double dialectVersion;
    private Map<String, ParsePathNode> branchMarkers = new HashMap<String, ParsePathNode>();
    private Set<String> branches = null;

    public ParserContext(PsiBuilder builder, DBLanguageDialect languageDialect, double version) {
        this.builder = new ParserBuilder(builder, languageDialect);
        this.dialectVersion = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ParserBuilder getBuilder() {
        return builder;
    }

    public double getDialectVersion() {
        return dialectVersion;
    }

    public void addBranchMarker(ParsePathNode parentNode, String branch) {
        branchMarkers.put(branch, parentNode);
        branches = branchMarkers.keySet();
    }

    public void removeBranchMarkers(ParsePathNode parentNode) {
        if (branchMarkers.size() > 0 && branchMarkers.containsValue(parentNode)) {
            Iterator<String> iterator = branchMarkers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (branchMarkers.get(key) == parentNode) {
                    iterator.remove();
                }
            }
        }
        branches = branchMarkers.size() == 0 ? null : branchMarkers.keySet();
    }

    public boolean checkBranches(ElementTypeRef elementTypeRef) {
        return branches == null || elementTypeRef.supportsBranches(branches);
    }

    @Override
    @Nullable
    public Set<String> getBranches() {
        return branches;
    }
}
