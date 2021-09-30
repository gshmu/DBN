package com.dci.intellij.dbn.diagnostics.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public final class DiagnosticBundle {
    private final DiagnosticType type;
    private final List<DiagnosticEntry> entries = new ArrayList<>();

    public DiagnosticBundle(DiagnosticType type) {
        this.type = type;
    }

    public void log(String identifier, boolean failure, boolean timeout, long executionTime) {
        DiagnosticEntry entry = ensure(identifier);
        entry.log(failure, timeout, executionTime);
    }

    @NotNull
    private DiagnosticEntry ensure(String identifier) {
        DiagnosticEntry record = find(identifier);
        if (record == null) {
            synchronized (this) {
                record = find(identifier);
                if (record == null) {
                    record = new DiagnosticEntry(identifier);
                    entries.add(record);
                }
            }
        }
        return record;
    }

    @Nullable
    private DiagnosticEntry find(String identifier) {
        return entries.stream().filter(entry -> Objects.equals(entry.getIdentifier(), identifier)).findFirst().orElseGet(() -> null);
    }

    public int size() {
        return entries.size();
    }

    @Override
    public String toString() {
        return type + " (" + size() + " entries)";
    }
}
