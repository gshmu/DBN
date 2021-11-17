package com.dci.intellij.dbn.diagnostics.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ParserDiagnosticsEntry implements Comparable<ParserDiagnosticsEntry>{
    public enum StateTransition {
        UNCHANGED,
        IMPROVED,
        DEGRADED,
        BROKEN,
        FIXED,
    }

    private final String filePath;
    private final int oldErrorCount;
    private final int newErrorCount;

    public ParserDiagnosticsEntry(String filePath, int oldErrorCount, int newErrorCount) {
        this.filePath = filePath;
        this.oldErrorCount = oldErrorCount;
        this.newErrorCount = newErrorCount;
    }

    public StateTransition getStateTransition() {
        if (newErrorCount == 0) {
            return StateTransition.FIXED;
        }

        if (oldErrorCount == 0) {
            return StateTransition.BROKEN;
        }

        if (newErrorCount > oldErrorCount) {
            return StateTransition.DEGRADED;
        }

        if (newErrorCount < oldErrorCount) {
            return StateTransition.IMPROVED;
        }

        return StateTransition.UNCHANGED;
    }

    @Override
    public int compareTo(@NotNull ParserDiagnosticsEntry o) {
        return filePath.compareTo(o.getFilePath());
    }
}
