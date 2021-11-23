package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        setDefaultSize(1200, 800);
        init();
        updateButtons();
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

    protected void updateButtons() {
        ParserDiagnosticsResult selectedResult = getForm().selectedResult();
        saveResultAction.setEnabled(selectedResult != null && selectedResult.isDraft());
        deleteResultAction.setEnabled(selectedResult != null && !selectedResult.isDraft());
        runDiagnosticsAction.setEnabled(!manager.hasDraftResults());
    }

    public void selectResult(@Nullable ParserDiagnosticsResult result) {
        getForm().selectResult(result);
    }

    private class SaveResultAction extends AbstractAction {
        private SaveResultAction() {
            super("Save Result");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ParserDiagnosticsResult selectedResult = getForm().selectedResult();
            if (selectedResult != null) {
                selectedResult.markSaved();
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
            if (selectedResult != null && !selectedResult.isDraft()) {
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
            Progress.modal(getProject(), "Running Parser Diagnostics", true, progress -> {
                ParserDiagnosticsResult result = manager.runParserDiagnostics(progress);
                form.refreshResults();
                form.selectResult(result);
                updateButtons();
            });
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
