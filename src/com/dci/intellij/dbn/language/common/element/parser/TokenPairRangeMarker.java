package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenPairRangeMarker {
    private final ParsePathNode parseNode;
    private final int offset;
    private boolean explicit;

    public TokenPairRangeMarker(ParsePathNode parseNode, PsiBuilder builder, boolean explicit) {
        this.parseNode = parseNode;
        this.offset = builder.getCurrentOffset();
        this.explicit = explicit;
    }

    @Override
    public String toString() {
        return offset + " " + explicit;
    }
}
