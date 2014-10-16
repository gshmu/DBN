package com.dci.intellij.dbn.editor.code.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;

public class CompileObjectAction extends AbstractSourceCodeEditorAction {
    public CompileObjectAction() {
        super("", "", Icons.OBEJCT_COMPILE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        FileEditor fileEditor = getFileEditor(e);
        if (virtualFile != null && fileEditor != null) {
            Project project = virtualFile.getProject();
            DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
            CompilerSettings compilerSettings = getCompilerSettings(project);
            DBContentType contentType = virtualFile.getContentType();
            CompilerAction compilerAction = new CompilerAction(CompilerActionSource.COMPILE, contentType, virtualFile, fileEditor);
            compilerManager.compileInBackground(
                    virtualFile.getObject(),
                    compilerSettings.getCompileType(),
                    compilerAction);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        if (virtualFile == null) {
            presentation.setEnabled(false);
        } else {

            DBSchemaObject schemaObject = virtualFile.getObject();
            if (schemaObject != null) {
                DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(schemaObject);
                if (schemaObject.getProperties().is(DBObjectProperty.COMPILABLE) &&  compatibilityInterface.supportsFeature(DatabaseFeature.OBJECT_INVALIDATION)) {
                    CompilerSettings compilerSettings = getCompilerSettings(schemaObject.getProject());
                    CompileType compileType = compilerSettings.getCompileType();
                    DBObjectStatusHolder status = schemaObject.getStatus();
                    DBContentType contentType = virtualFile.getContentType();

                    boolean isDebug = compileType == CompileType.DEBUG;
                    if (compileType == CompileType.KEEP) {
                        isDebug = status.is(contentType, DBObjectStatus.DEBUG);
                    }

                    boolean isPresent = status.is(contentType, DBObjectStatus.PRESENT);
                    boolean isValid = status.is(contentType, DBObjectStatus.VALID);
                    boolean isModified = virtualFile.isModified();

                    boolean isCompiling = status.is(contentType, DBObjectStatus.COMPILING);
                    boolean isEnabled = !isModified && isPresent && !isCompiling && (compilerSettings.alwaysShowCompilerControls() || !isValid /*|| isDebug != isDebugActive*/);

                    presentation.setEnabled(isEnabled);
                    String text =
                            contentType == DBContentType.CODE_SPEC ? "Compile spec" :
                                    contentType == DBContentType.CODE_BODY ? "Compile body" : "Compile";

                    if (isDebug) text = text + " (Debug)";
                    if (compileType == CompileType.ASK) text = text + "...";

                    presentation.setVisible(true);
                    presentation.setText(text);

                    Icon icon = isDebug ?
                            CompileType.DEBUG.getIcon() :
                            CompileType.NORMAL.getIcon();
                    presentation.setIcon(icon);
                } else {
                    presentation.setVisible(false);
                }
            } else {
                presentation.setVisible(false);
            }
        }
    }

    private CompilerSettings getCompilerSettings(Project project) {
        return ExecutionEngineSettings.getInstance(project).getCompilerSettings();
    }
}
