package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;

public interface ElementLookupContext {

    @Nullable
    Set<String> getBranches();

    boolean checkBranches(ElementTypeRef elementTypeRef);
}
