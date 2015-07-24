package com.dci.intellij.dbn.execution.method.result.action;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class CursorResultFetchNextRecordsAction extends MethodExecutionCursorResultAction {
    public CursorResultFetchNextRecordsAction() {
        super("Fetch Next Records", Icons.EXEC_RESULT_RESUME);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final ResultSetTable resultSetTable = getResultSetTable(e);
        if (project != null && resultSetTable != null) {
            new BackgroundTask(project, "Loading cursor result records", false) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    try {
                        ResultSetDataModel model = resultSetTable.getModel();
                        if (!model.isResultSetExhausted()) {
                            ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(project);
                            int fetchBlockSize = settings.getStatementExecutionSettings().getResultSetFetchBlockSize();

                            model.fetchNextRecords(fetchBlockSize, false);
                        }

                    } catch (SQLException ex) {
                        MessageUtil.showErrorDialog(project, "Could not perform operation.", ex);
                    }
                }
            }.start();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Fetch Next Records");
        if (resultSetTable != null) {
            ResultSetDataModel model = resultSetTable.getModel();
            boolean enabled = !model.isResultSetExhausted();
            presentation.setEnabled(enabled);
        } else {
            presentation.setEnabled(false);
        }
    }
}
