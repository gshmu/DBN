package com.dci.intellij.dbn.diagnostics.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParserDiagnosticsUtil {
    private ParserDiagnosticsUtil() {}

    @NotNull
    public static StateTransition computeStateTransition(
            @Nullable IssueCounter oldIssues,
            @Nullable IssueCounter newIssues) {

        int oldCount = oldIssues == null ? 0 : oldIssues.issueCount();
        int newCount = newIssues == null ? 0 : newIssues.issueCount();
        if (newCount == 0) {
            return StateTransition.FIXED;
        }

        if (oldCount == 0) {
            return StateTransition.BROKEN;
        }

        if (newCount > oldCount) {
            return StateTransition.DEGRADED;
        }

        if (newCount < oldCount) {
            return StateTransition.IMPROVED;
        }

        return StateTransition.UNCHANGED;
    }
}
