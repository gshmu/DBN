package com.dci.intellij.dbn.diagnostics.data;

import org.jetbrains.annotations.NotNull;

public final class ParserDiagnosticsUtil {
    private ParserDiagnosticsUtil() {}

    @NotNull
    public static StateTransition computeStateTransition(int oldErrorCount, int newErrorCount) {
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
}
