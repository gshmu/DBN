package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenPairMarker {
    private final ParsePathNode parseNode;
    private final int offset;
    private boolean explicit;

    public TokenPairMarker(ParsePathNode parseNode, int offset, boolean explicit) {
        this.parseNode = parseNode;
        this.offset = offset;
        this.explicit = explicit;
    }

    @Override
    public String toString() {
        return offset + " " + explicit;
    }
}
