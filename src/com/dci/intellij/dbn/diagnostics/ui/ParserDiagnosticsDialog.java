package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class ParserDiagnosticsDialog extends DBNDialog<ParserDiagnosticsForm> {
    private final SaveResultAction saveResultAction = new SaveResultAction();
    private final DeleteResultAction deleteResultAction = new DeleteResultAction();
    private final RunDiagnosticsAction runDiagnosticsAction = new RunDiagnosticsAction();
    private final ParserDiagnosticsManager manager;


    public ParserDiagnosticsDialog(Project project) {
        super(project, "Diagnostics Monitor", true);
        manager = ParserDiagnosticsManager.getInstance(getProject());
        setModal(false);
        setResizable(true);
        setCancelButtonText("Close");
        init();
    }

    public void initResult(@NotNull ParserDiagnosticsResult current) {
        getForm().initResult(current);
        runDiagnosticsAction.setEnabled(!current.isSaved());
    }

    @NotNull
    @Override
    protected ParserDiagnosticsForm createForm() {
        return new ParserDiagnosticsForm(this);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                saveResultAction,
                deleteResultAction,
                runDiagnosticsAction,
                getCancelAction(),
                getHelpAction()
        };
    }

    public void updateButtons() {
        ParserDiagnosticsResult selectedResult = getForm().selectedResult();
        saveResultAction.setEnabled(selectedResult != null && !selectedResult.isSaved());
        deleteResultAction.setEnabled(selectedResult != null && selectedResult.isSaved());
    }

    private class SaveResultAction extends AbstractAction {
        private SaveResultAction() {
            super("Save Result");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ParserDiagnosticsResult selectedResult = getForm().selectedResult();
            if (selectedResult != null) {
                manager.saveResult(selectedResult);
                updateButtons();
            }
        }
    }

    private class DeleteResultAction extends AbstractAction {
        private DeleteResultAction() {
            super("Delete Result");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ParserDiagnosticsForm form = getForm();
            ParserDiagnosticsResult selectedResult = form.selectedResult();
            if (selectedResult != null && selectedResult.isSaved()) {
                manager.deleteResult(selectedResult);
                form.refreshResults();
                updateButtons();
            }
        }
    }

    private class RunDiagnosticsAction extends AbstractAction {
        private RunDiagnosticsAction() {
            super("Run Diagnostics");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ParserDiagnosticsForm form = getForm();
            ParserDiagnosticsResult selectedResult = form.selectedResult();
            if (selectedResult != null) {
                Progress.modal(getProject(), "Running Parser Diagnostics", true, progress -> {
                    ParserDiagnosticsResult result = manager.runParserDiagnostics(progress);
                    form.addResult(result);
                    updateButtons();
                });

            }
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
