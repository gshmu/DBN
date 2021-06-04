package com.dci.intellij.dbn.code.common.completion.options.filter.ui;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterOption;
import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterOptionBundle;
import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class CodeCompletionFilterTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer { //implements TreeCellEditor {
    public static final CodeCompletionFilterTreeCellRenderer CELL_RENDERER = new CodeCompletionFilterTreeCellRenderer();

    @Override
    public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof CodeCompletionFilterOptionBundle) {
            CodeCompletionFilterOptionBundle optionBundle = (CodeCompletionFilterOptionBundle) userObject;
            getTextRenderer().append(optionBundle.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        }
        else if(userObject instanceof CodeCompletionFilterOption) {
            CodeCompletionFilterOption option = (CodeCompletionFilterOption) userObject;
            Icon icon = option.getIcon();
            getTextRenderer().append(option.getName(), icon == null ?
                    SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES :
                    SimpleTextAttributes.REGULAR_ATTRIBUTES);
            getTextRenderer().setIcon(icon);
        }
        else if (userObject instanceof CodeCompletionFilterSettings){
            CodeCompletionFilterSettings codeCompletionFilterSettings = (CodeCompletionFilterSettings) userObject;
            getTextRenderer().append(codeCompletionFilterSettings.getDisplayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }

        setBackground(UIUtil.getTextFieldBackground());
    }
}

