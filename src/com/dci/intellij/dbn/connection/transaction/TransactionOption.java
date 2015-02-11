package com.dci.intellij.dbn.connection.transaction;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.option.InteractiveOption;

public enum TransactionOption implements InteractiveOption{
    ASK("Ask", null, true),
    COMMIT("Commit", Icons.CONNECTION_COMMIT, true),
    ROLLBACK("Rollback", Icons.CONNECTION_ROLLBACK, true),
    REVIEW_CHANGES("Review changes", null, true),
    CANCEL("Cancel", null, false)
    ;

    private String name;
    private Icon icon;
    private boolean persistable;

    TransactionOption(String name, Icon icon, boolean persistable) {
        this.name = name;
        this.icon = icon;
        this.persistable = persistable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public boolean isPersistable() {
        return persistable;
    }
}
