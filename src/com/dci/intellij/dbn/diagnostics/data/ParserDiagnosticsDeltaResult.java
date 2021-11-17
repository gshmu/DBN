package com.dci.intellij.dbn.diagnostics.data;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class ParserDiagnosticsDeltaResult {
    private final List<ParserDiagnosticsEntry> entries = new ArrayList<>();

    public void addEntry(String file, int oldErrorCount, int newErrorCount) {
        ParserDiagnosticsEntry diagnosticsCapture = new ParserDiagnosticsEntry(file, oldErrorCount, newErrorCount);
        entries.add(diagnosticsCapture);
    }
}
