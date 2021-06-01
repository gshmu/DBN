package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelListener;

public abstract class DiagnosticsTableModel implements DBNTableModel, Disposable {
    private final ProjectRef project;
    private final Latent<DiagnosticBundle> diagnostics = Latent.basic(() -> resolveDiagnostics());

    public DiagnosticsTableModel(Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    protected abstract String[] getColumnNames();

    @NotNull
    protected abstract DiagnosticBundle resolveDiagnostics();

    protected abstract Comparable getColumnValue(DiagnosticEntry entry, int column);

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public DiagnosticBundle getDiagnostics() {
        return diagnostics.get();
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
    public final boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        return getDiagnostics().getEntries().get(rowIndex);
    }

    @Override
    public final void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

    @Override
    public final void addTableModelListener(TableModelListener l) {}

    @Override
    public final void removeTableModelListener(TableModelListener l) {}

    @Override
    public void dispose() {
    }
}
