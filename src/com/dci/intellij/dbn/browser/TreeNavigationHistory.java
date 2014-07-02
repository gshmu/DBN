package com.dci.intellij.dbn.browser;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;

import java.util.ArrayList;
import java.util.List;

public class TreeNavigationHistory{
    private List<BrowserTreeNode> history = new ArrayList<BrowserTreeNode>();
    private int offset;

    public void add(BrowserTreeNode treeNode) {
        if (history.size() > 0 && treeNode == history.get(offset)) {
            return;
        }
        while (history.size() - 1  > offset) {
            history.remove(offset);
        }

        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(treeNode.getProject());

        int historySize = browserSettings.getGeneralSettings().getNavigationHistorySize().value();
        while (history.size() > historySize) {
            history.remove(0);
        }
        history.add(treeNode);
        offset = history.size() -1;
    }

    public void clear() {
        history.clear();
    }

    public boolean hasNext() {
        return offset < history.size()-1;
    }

    public boolean hasPrevious() {
        return offset > 0;
    }

    public BrowserTreeNode next() {
        offset = offset + 1;
        return history.get(offset);
    }

    public BrowserTreeNode previous() {
        offset = offset -1;
        return history.get(offset);
    }

}
