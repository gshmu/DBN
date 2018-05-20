package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import org.jetbrains.annotations.NotNull;

public interface MessageTreeNode extends MessagesTreeNode{
    @NotNull
    ConsoleMessage getMessage();
}
