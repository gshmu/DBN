package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.execution.statement.action.StatementGutterAction;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class StatementGutterRenderer extends GutterIconRenderer {
    private StatementGutterAction action;
    public StatementGutterRenderer(StatementExecutionProcessor executionProcessor) {
        this.action = new StatementGutterAction(executionProcessor);
    }

    @NotNull
    public Icon getIcon() {
        return action.getIcon();
    }

    public boolean isNavigateAction() {
        return true;
    }

    @Nullable
    public synchronized AnAction getClickAction() {
        return action;
    }

    @Nullable
    public String getTooltipText() {
        return action.getTooltipText();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatementGutterRenderer) {
            StatementGutterRenderer renderer = (StatementGutterRenderer) obj;
            return action.equals(renderer.action);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return action.hashCode();
    }

    
}
