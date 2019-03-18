package com.dci.intellij.dbn.execution.common.message.ui.tree.node;

import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTreeLeafNode;
import com.dci.intellij.dbn.execution.method.MethodExecutionMessage;

class MethodExecutionMessageNode extends MessagesTreeLeafNode<MethodExecutionMessagesObjectNode, MethodExecutionMessage> {

    MethodExecutionMessageNode(MethodExecutionMessagesObjectNode parent, MethodExecutionMessage methodExecutionMessage) {
        super(parent, methodExecutionMessage);
    }
}