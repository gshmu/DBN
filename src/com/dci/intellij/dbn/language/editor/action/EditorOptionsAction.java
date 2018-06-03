package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.OpenSettingsAction;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class EditorOptionsAction extends GroupPopupAction {
    public EditorOptionsAction() {
        super("Options", "Options", Icons.ACTION_OPTIONS);
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        List<AnAction> actions = new ArrayList<AnAction>();
        VirtualFile virtualFile = getVirtualFile(e);
        if (virtualFile instanceof DBConsoleVirtualFile) {
            actions.add(new ConsoleRenameAction());
            actions.add(new ConsoleDeleteAction());
            actions.add(new ConsoleSaveToFileAction());
            actions.add(Separator.getInstance());

            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
            if (consoleVirtualFile.getType() != DBConsoleType.DEBUG) {
                actions.add(new ConsoleCreateAction(DBConsoleType.STANDARD));
            }

            ConnectionHandler connectionHandler = consoleVirtualFile.getConnectionHandler();
            if (DatabaseFeature.DEBUGGING.isSupported(connectionHandler)) {
                actions.add(new ConsoleCreateAction(DBConsoleType.DEBUG));
            }
        }
        actions.add(Separator.getInstance());
        actions.add(new OpenSettingsAction(ConfigId.CODE_EDITOR, false));

        return actions.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        VirtualFile virtualFile = getVirtualFile(e);
        presentation.setVisible(virtualFile instanceof DBConsoleVirtualFile);
    }
}
