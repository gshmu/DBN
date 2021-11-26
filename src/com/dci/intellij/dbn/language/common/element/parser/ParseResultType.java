package com.dci.intellij.dbn.language.common.element.parser;

import lombok.Getter;

@Getter
public enum ParseResultType {
    FULL_MATCH (2),
    PARTIAL_MATCH(1),
    NO_MATCH(0);

    private final int score;

    ParseResultType(int score) {
        this.score = score;
    }

}
