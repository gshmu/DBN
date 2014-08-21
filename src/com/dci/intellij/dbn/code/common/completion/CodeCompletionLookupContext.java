package com.dci.intellij.dbn.code.common.completion;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;

public class CodeCompletionLookupContext implements ElementLookupContext {
    private Set<String> branches;

    public CodeCompletionLookupContext(Set<String> branches) {
        this.branches = branches;
    }

    @Override
    @Nullable
    public Set<String> getBranches() {
        return branches;
    }

    @Override
    public boolean checkBranches(ElementTypeRef elementTypeRef) {
        return branches == null || elementTypeRef.supportsBranches(branches);
    }
}
