package com.dci.intellij.dbn.execution.common.message.ui.tree;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;

public interface MessageTreeNode extends MessagesTreeNode{
    @NotNull
    ConsoleMessage getMessage();
}
