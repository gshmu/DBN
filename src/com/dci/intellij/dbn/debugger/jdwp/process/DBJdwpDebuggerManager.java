package com.dci.intellij.dbn.debugger.jdwp.process;

import com.intellij.debugger.impl.DebuggerManagerImpl;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;

public class DBJdwpDebuggerManager extends DebuggerManagerImpl {
    public DBJdwpDebuggerManager(Project project, StartupManager startupManager, EditorColorsManager colorsManager) {
        super(project, startupManager, colorsManager);
    }
}
