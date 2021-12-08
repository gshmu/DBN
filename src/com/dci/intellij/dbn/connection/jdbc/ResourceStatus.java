package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
import lombok.Getter;

@Getter
public enum ResourceStatus implements Property.IntBase {
    // shared
    CLOSED,
    CLOSED_CHECKING(true),
    CLOSED_APPLYING(true),

    VALID,
    VALID_CHECKING(true),
    VALID_APPLYING(true),

    // statement
    CANCELLED,
    CANCELLED_CHECKING(true),
    CANCELLED_APPLYING(true),

    // connection
    ACTIVE,
    RESERVED,

    AUTO_COMMIT,
    AUTO_COMMIT_CHECKING(true),
    AUTO_COMMIT_APPLYING(true),

    // transient statuses
    CLOSING(true),
    CANCELLING(true),
    COMMITTING(true),
    ROLLING_BACK(true),

    CREATING_SAVEPOINT(true),
    COMMITTING_SAVEPOINT(true),
    ROLLING_BACK_SAVEPOINT(true),
    RELEASING_SAVEPOINT(true),

    CHANGING_READONLY_STATUS(true),
    CHANGING_AUTOCOMMIT_STATUS(true),

    RESOLVING_TRANSACTION(true);

    private final boolean transitory;
    private final Computed computed = new Computed(this);

    ResourceStatus() {
        this(false);
    }

    ResourceStatus(boolean transitory) {
        this.transitory = transitory;
    }

    @Override
    public Computed computed() {
        return computed;
    }
}
