package com.dci.intellij.dbn.code.common.completion.options.filter;

import com.dci.intellij.dbn.code.common.completion.options.filter.ui.CheckedTreeNodeProvider;
import com.dci.intellij.dbn.code.common.completion.options.filter.ui.CodeCompletionFilterTreeNode;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.intellij.ui.CheckedTreeNode;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class CodeCompletionFilterOptionBundle implements CheckedTreeNodeProvider, PersistentConfiguration {
    private List<CodeCompletionFilterOption> options = new ArrayList<CodeCompletionFilterOption>();
    private CodeCompletionFilterSettings filterSettings;
    private String name;

    public CodeCompletionFilterOptionBundle(String name, CodeCompletionFilterSettings filterSettings) {
        this.name = name;
        this.filterSettings = filterSettings;
    }

    public List<CodeCompletionFilterOption> getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    public CodeCompletionFilterSettings getFilterSettings() {
        return filterSettings;
    }

    @Override
    public void readConfiguration(Element element) {
        List children = element.getChildren();
        for (Object child: children) {
            CodeCompletionFilterOption option = new CodeCompletionFilterOption(filterSettings);
            Element childElement = (Element) child;
            if (childElement.getName().equals("filter-element")){
                option.readConfiguration(childElement);
                int index = options.indexOf(option);
                if (index == -1) {
                    options.add(option);
                } else {
                    option = options.get(index);
                    option.readConfiguration(childElement);
                }
            }
        }
    }

    @Override
    public void writeConfiguration(Element element){
        for (CodeCompletionFilterOption option : options) {
            Element child = new Element("filter-element");
            option.writeConfiguration(child);
            element.addContent(child);
        }
    }

    @Override
    public CheckedTreeNode createCheckedTreeNode() {
        CodeCompletionFilterTreeNode node = new CodeCompletionFilterTreeNode(this, false);
        //node.setChecked(true);
        for (CodeCompletionFilterOption option : options) {
            //if (!option.isSelected()) node.setChecked(false);
            node.add(option.createCheckedTreeNode());
        }
        node.updateCheckedStatusFromChildren();
        return node;
    }
}
