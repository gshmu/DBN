package com.dci.intellij.dbn.object.common.loader;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionLoadListener;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DatabaseLoaderManager extends AbstractProjectComponent {
    private DatabaseLoaderQueue loaderQueue;

    private DatabaseLoaderManager(final Project project) {
        super(project);
        EventUtil.subscribe(project, this, ConnectionLoadListener.TOPIC, new ConnectionLoadListener() {
            @Override
            public void contentsLoaded(final ConnectionHandler connectionHandler) {
                new SimpleLaterInvocator() {
                    @Override
                    protected void execute() {
                        if (!project.isDisposed()) {
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
                            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
                            for (VirtualFile openFile : openFiles) {

                                ConnectionHandler activeConnection = connectionMappingManager.getConnectionHandler(openFile);
                                if (activeConnection == connectionHandler) {
                                    FileEditor[] fileEditors = fileEditorManager.getEditors(openFile);
                                    for (FileEditor fileEditor : fileEditors) {
                                        Editor editor = EditorUtil.getEditor(fileEditor);

                                        if (editor != null) {
                                            DocumentUtil.refreshEditorAnnotations(editor);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }.start();

            }
        });
    }

    public static DatabaseLoaderManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseLoaderManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseLoaderManager";
    }

    public void dispose() {
        super.dispose();
        if (loaderQueue != null) {
            Disposer.dispose(loaderQueue);
            loaderQueue = null;
        }
    }
}
