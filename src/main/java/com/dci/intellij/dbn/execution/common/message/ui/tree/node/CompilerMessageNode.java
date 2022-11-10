package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeLeafNode;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import org.jetbrains.annotations.Nullable;

public class CompilerMessageNode extends MessagesTreeLeafNode<CompilerMessagesObjectNode, CompilerMessage> {

    CompilerMessageNode(CompilerMessagesObjectNode parent, CompilerMessage compilerMessage) {
        super(parent, compilerMessage);
    }

    @Nullable
    @Override
    public DBContentVirtualFile getVirtualFile() {
        return getMessage().getContentFile();
    }

    @Override
    public String toString() {
        CompilerMessage compilerMessage = getMessage();
        return "[" + compilerMessage.getType() + "] " + compilerMessage.getText();
    }
}