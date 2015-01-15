package com.dci.intellij.dbn.debugger;

import org.jetbrains.annotations.NotNull;

import com.intellij.debugger.ui.DebuggerContentInfo;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.ui.XDebugTabLayouter;

public class DBProgramDebugTabLayouter extends XDebugTabLayouter {
    @NotNull
    @Override
    public Content registerConsoleContent(@NotNull RunnerLayoutUi ui, @NotNull ExecutionConsole console) {
        Content content = ui.createContent(DebuggerContentInfo.CONSOLE_CONTENT, console.getComponent(),
                XDebuggerBundle.message("debugger.session.tab.console.content.name"),
                AllIcons.Debugger.Console,
                console.getPreferredFocusableComponent());
        content.setCloseable(false);
        ui.addContent(content, 0, PlaceInGrid.bottom, false);
        return content;
    }
}
