package com.dci.intellij.dbn.diagnostics.data;

import com.dci.intellij.dbn.common.util.Lists;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public final class DiagnosticBundle {
    private final DiagnosticType type;
    private final List<DiagnosticEntry> entries = new CopyOnWriteArrayList<>();

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
        return Lists.first(entries, entry -> Objects.equals(entry.getIdentifier(), identifier));
    }

    public int size() {
        return entries.size();
    }

    @Override
    public String toString() {
        return type + " (" + size() + " entries)";
    }
}
