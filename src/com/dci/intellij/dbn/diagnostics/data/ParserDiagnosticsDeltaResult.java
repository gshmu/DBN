package com.dci.intellij.dbn.diagnostics.data;


import com.dci.intellij.dbn.common.list.FilteredList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsUtil.computeStateTransition;


@Getter
public class ParserDiagnosticsDeltaResult {
    private ParserDiagnosticsFilter filter = ParserDiagnosticsFilter.EMPTY;
    private final List<ParserDiagnosticsEntry> entries = FilteredList.stateful(filter);

    private final ParserDiagnosticsResult previous;
    private final ParserDiagnosticsResult current;

    public ParserDiagnosticsDeltaResult(@Nullable ParserDiagnosticsResult previous, @NotNull ParserDiagnosticsResult current) {
        this.previous = previous;
        this.current = current;
        current.getEntries().keySet().forEach(file -> {
            int newErrorCount = current.getErrorCount(file);
            int oldErrorCount = previous == null ? newErrorCount : previous.getErrorCount(file);
            addEntry(file, oldErrorCount, newErrorCount);
        });

        if (previous != null) {
            previous.getFiles().stream().filter(file -> !current.isPresent(file)).forEach(file ->
                    addEntry(file, previous.getErrorCount(file), 0));
        }
    }

    public void setFilter(ParserDiagnosticsFilter filter) {
        this.filter = filter;
    }

    private void addEntry(String file, int oldErrorCount, int newErrorCount) {
        ParserDiagnosticsEntry diagnosticsCapture = new ParserDiagnosticsEntry(file, oldErrorCount, newErrorCount);
        entries.add(diagnosticsCapture);
    }

    public String getName() {
        if (previous == null) {
            return current.getName();
        } else {
            return current.getName() + " compared to " + previous.getName();
        }
    }

    public StateTransition getFilter() {
        int oldErrorCount = previous == null ? current.getErrorCount() : previous.getErrorCount();
        int newErrorCount = current.getErrorCount();

        return computeStateTransition(oldErrorCount, newErrorCount);
    }
}
