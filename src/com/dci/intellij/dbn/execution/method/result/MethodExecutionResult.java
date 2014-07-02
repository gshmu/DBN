package com.dci.intellij.dbn.execution.method.result;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBArgumentRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExecutionResult implements ExecutionResult, Disposable {
    private MethodExecutionInput executionInput;
    private MethodExecutionResultForm resultPanel;
    private List<ArgumentValue> argumentValues = new ArrayList<ArgumentValue>();
    private Map<DBArgumentRef, ResultSetDataModel> cursorModels;
    private int executionDuration;
    private boolean debug;

    public MethodExecutionResult(MethodExecutionInput executionInput, boolean debug) {
        this.executionInput = executionInput;
        executionInput.setExecutionResult(this);
        this.debug = debug;
    }

    public MethodExecutionResult(MethodExecutionInput executionInput, MethodExecutionResultForm resultPanel, boolean debug) {
        this(executionInput, debug);
        this.resultPanel = resultPanel;
    }

    public int getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(int executionDuration) {
        this.executionDuration = executionDuration;
    }

    public void addArgumentValue(DBArgument argument, Object value) throws SQLException {
        ArgumentValue argumentValue = new ArgumentValue(argument, value);
        argumentValues.add(argumentValue);
        if (value instanceof ResultSet) {
            ResultSet resultSet = (ResultSet) value;
            if (cursorModels == null) {
                cursorModels = new HashMap<DBArgumentRef, ResultSetDataModel>();
            }

            ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(argument.getProject());
            int maxRecords = settings.getStatementExecutionSettings().getResultSetFetchBlockSize();
            ResultSetDataModel dataModel = new ResultSetDataModel(resultSet, getConnectionHandler(), maxRecords);
            cursorModels.put(argument.getRef(), dataModel);
        }
    }

    public void addArgumentValue(DBArgument argument, DBTypeAttribute attribute, Object value) {
        ArgumentValue argumentValue = new ArgumentValue(argument, attribute, value);
        argumentValues.add(argumentValue);
    }


    public List<ArgumentValue> getArgumentValues() {
        return argumentValues;
    }

    public MethodExecutionResultForm getResultPanel() {
        if (resultPanel == null) {
            resultPanel = new MethodExecutionResultForm(this);
        }
        return resultPanel;
    }

    public String getResultName() {
        return getMethod().getName();
    }

    public Icon getResultIcon() {
        return getMethod().getOriginalIcon();
    }

    public boolean isOrphan() {
        return false;
    }

    public MethodExecutionInput getExecutionInput() {
        return executionInput;
    }

    public DBMethod getMethod() {
        return executionInput.getMethod();
    }

    public Project getProject() {
        return getMethod().getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return getMethod().getConnectionHandler();
    }

    public boolean hasCursorResults() {
        for (ArgumentValue argumentValue: argumentValues) {
            if (argumentValue.isCursor()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSimpleResults() {
        for (ArgumentValue argumentValue: argumentValues) {
            if (!argumentValue.isCursor()) {
                return true;
            }
        }
        return false;
    }


    public void dispose() {
        resultPanel = null;
        executionInput.setExecutionResult(null);
        executionInput = null;
        if (cursorModels != null) {
            for (ResultSetDataModel resultSetDataModel : cursorModels.values()) {
                resultSetDataModel.dispose();
            }
            cursorModels.clear();
        }
        argumentValues.clear();
    }

    public void setResultPanel(MethodExecutionResultForm resultPanel) {
        this.resultPanel = resultPanel;
    }

    public boolean isDebug() {
        return debug;
    }

    public ResultSetDataModel getTableModel(DBArgument argument) {
        return cursorModels.get(argument.getRef());
    }
}
