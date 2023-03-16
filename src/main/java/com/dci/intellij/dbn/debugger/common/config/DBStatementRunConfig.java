package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DBStatementRunConfig extends DBRunConfig<StatementExecutionInput> {
    private StatementExecutionInput executionInput;

    public DBStatementRunConfig(Project project, DBStatementRunConfigFactory factory, String name, DBRunConfigCategory category) {
        super(project, factory, name, category);
    }

    @Nullable
    @Override
    public DBLanguagePsiFile getSource() {
        if (executionInput != null) {
            return executionInput.getExecutionProcessor().getPsiFile();
        }
        return null;
    }

    @Override
    public List<DBMethod> getMethods() {
        if (executionInput != null) {
            ExecutablePsiElement executablePsiElement = executionInput.getExecutionProcessor().getCachedExecutable();
            if (executablePsiElement == null) return Collections.emptyList();

            return Read.call(executablePsiElement, e -> {
                List<DBMethod> methods = new ArrayList<>();
                e.collectObjectReferences(DBObjectType.METHOD, object -> {
                    if (object instanceof DBMethod) {
                        DBMethod method = (DBMethod) object;
                        DBSchema schema = method.getSchema();
                        if (!schema.isSystemSchema() && !schema.isPublicSchema()) {
                            methods.add(method);
                        }
                    }
                });
                return methods;
            });

        }
        return Collections.emptyList();
    }

    @Override
    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    @Override
    public void setExecutionInput(StatementExecutionInput executionInput) {
        this.executionInput = executionInput;
    }

    @Override
    @Nullable
    public String suggestedName() {
        if (getCategory() == DBRunConfigCategory.GENERIC) {
            String defaultRunnerName = getType().getDefaultRunnerName();
            if (getDebuggerType() == DBDebuggerType.JDWP) {
                defaultRunnerName = defaultRunnerName + " (JDWP)";
            }
            return defaultRunnerName;
        }
        return null;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

    }
}
