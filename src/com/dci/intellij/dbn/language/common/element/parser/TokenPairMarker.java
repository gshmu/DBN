package com.dci.intellij.dbn.language.common.element.parser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenPairMarker {
    private final int offset;
    private boolean explicit;

    public TokenPairMarker(int offset, boolean explicit) {
        this.offset = offset;
        this.explicit = explicit;
    }

    @Override
    public String toString() {
        return offset + " " + explicit;
    }
}
