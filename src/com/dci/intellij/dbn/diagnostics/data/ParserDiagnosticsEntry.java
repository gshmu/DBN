package com.dci.intellij.dbn.diagnostics.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsUtil.computeStateTransition;

@Getter
public class ParserDiagnosticsEntry implements Comparable<ParserDiagnosticsEntry>{

    private final String filePath;
    private final int oldErrorCount;
    private final int newErrorCount;

    public ParserDiagnosticsEntry(String filePath, int oldErrorCount, int newErrorCount) {
        this.filePath = filePath;
        this.oldErrorCount = oldErrorCount;
        this.newErrorCount = newErrorCount;
    }

    public StateTransition getStateTransition() {
        return computeStateTransition(oldErrorCount, newErrorCount);
    }

    @Override
    public int compareTo(@NotNull ParserDiagnosticsEntry o) {
        return filePath.compareTo(o.getFilePath());
    }
}
