package com.dci.intellij.dbn.diagnostics.ui.model;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.ui.table.DBNMutableTableModel;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDiagnosticsTableModel<T> extends DBNMutableTableModel<DiagnosticEntry<T>> {
    private final ProjectRef project;
    private final Latent<DiagnosticBundle<T>> diagnostics = Latent.weak(() -> resolveDiagnostics());
    private transient int signature = 0;

    public AbstractDiagnosticsTableModel(Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    protected abstract String[] getColumnNames();

    @NotNull
    protected abstract DiagnosticBundle<T> resolveDiagnostics();

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public DiagnosticBundle<T> getDiagnostics() {
        DiagnosticBundle<T> diagnostics = this.diagnostics.get();
        int newSignature = diagnostics.getSignature();
        if (this.signature != newSignature) {
            this.signature = newSignature;
            notifyRowChanges();
        }
        return diagnostics;
    }

    @Override
    public final int getRowCount() {
        return getDiagnostics().getEntries().size();
    }

    @Override
    public final int getColumnCount() {
        return getColumnNames().length;
    }

    @Override
    public final String getColumnName(int columnIndex) {
        return getColumnNames()[columnIndex];
    }

    @Override
    public final Class<?> getColumnClass(int columnIndex) {
        return DiagnosticEntry.class;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        return getDiagnostics().getEntries().get(rowIndex);
    }
}
