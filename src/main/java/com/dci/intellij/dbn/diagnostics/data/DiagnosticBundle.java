package com.dci.intellij.dbn.diagnostics.data;

import com.dci.intellij.dbn.common.util.Lists;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public final class DiagnosticBundle<T> {
    private final DiagnosticType type;
    private final List<DiagnosticEntry<T>> entries = new CopyOnWriteArrayList<>();
    private transient int signature;

    public DiagnosticBundle(DiagnosticType type) {
        this.type = type;
    }

    public void log(T identifier, boolean failure, boolean timeout, long executionTime) {
        DiagnosticEntry<T> entry = ensure(identifier);
        entry.log(failure, timeout, executionTime);
        signature = Objects.hash(entries.toArray());
    }

    @NotNull
    private DiagnosticEntry<T> ensure(T identifier) {
        DiagnosticEntry<T> record = find(identifier);
        if (record == null) {
            synchronized (this) {
                record = find(identifier);
                if (record == null) {
                    record = new DiagnosticEntry<T>(identifier);
                    entries.add(record);
                }
            }
        }
        return record;
    }

    @Nullable
    private DiagnosticEntry<T> find(T identifier) {
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
