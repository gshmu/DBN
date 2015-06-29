package com.dci.intellij.dbn.debugger.execution.method;

import javax.swing.Icon;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.xdebugger.XDebugSession;

public class DBMethodDebugProcess extends DBProgramDebugProcess<MethodExecutionInput>{
    public DBMethodDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session, connectionHandler);
    }

    @Override
    protected void doExecuteTarget() throws SQLException {
        MethodExecutionInput methodExecutionInput = getExecutionInput();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        methodExecutionManager.debugExecute(methodExecutionInput, getTargetConnection());
    }

    @Override
    protected boolean isTerminated() {
        return super.isTerminated() || getRuntimeInfo().getOwnerName() == null;
    }

    @Override
    protected void registerDefaultBreakpoint() {
            MethodExecutionInput methodExecutionInput = getExecutionInput();
            DBMethod method = methodExecutionInput.getMethod();
            DBEditableObjectVirtualFile mainDatabaseFile = getMainDatabaseFile();
            if (method != null && mainDatabaseFile != null) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) mainDatabaseFile.getMainContentFile();
                PSQLFile psqlFile = (PSQLFile) sourceCodeFile.getPsiFile();
                if (psqlFile != null) {
                    BasePsiElement basePsiElement = psqlFile.lookupObjectDeclaration(method.getObjectType().getGenericType(), method.getName());
                    if (basePsiElement != null) {
                        BasePsiElement subject = basePsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                        int offset = subject.getTextOffset();
                        Document document = DocumentUtil.getDocument(psqlFile);
                        int line = document.getLineNumber(offset);

                        DBSchemaObject schemaObject = getMainDatabaseObject();
                        if (schemaObject != null) {
                            try {
                                defaultBreakpointInfo = getDebuggerInterface().addProgramBreakpoint(
                                        method.getSchema().getName(),
                                        schemaObject.getName(),
                                        schemaObject.getObjectType().getName().toUpperCase(),
                                        line,
                                        getDebugConnection());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        }
    }

    @Nullable
    protected DBEditableObjectVirtualFile getMainDatabaseFile() {
        DBSchemaObject schemaObject = getMainDatabaseObject();
        return schemaObject == null ? null : schemaObject.getVirtualFile();
    }

    @Nullable
    public DBSchemaObject getMainDatabaseObject() {
        MethodExecutionInput methodExecutionInput = getExecutionInput();
        DBMethod method = methodExecutionInput.getMethod();
        return method != null && method.isProgramMethod() ? method.getProgram() : method;
    }


    @NotNull
    @Override
    public String getName() {
        DBSchemaObject object = getMainDatabaseObject();
        if (object != null) {
            return object.getQualifiedName();
        }
        return "Debug Process";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        DBSchemaObject object = getMainDatabaseObject();
        if (object != null) {
            return object.getIcon();
        }
        return null;
    }
}
