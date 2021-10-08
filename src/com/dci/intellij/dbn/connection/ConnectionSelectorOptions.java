package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class ConnectionSelectorOptions extends PropertyHolderImpl<ConnectionSelectorOptions.Option> {

    @Override
    protected Option[] properties() {
        return Option.values();
    }

    public enum Option implements Property {
        SHOW_VIRTUAL_CONNECTIONS,
        SHOW_CREATE_CONNECTION,
        PROMPT_SCHEMA_SELECTION;

        private final Computed computed = new Computed(this);

        @Override
        public Computed computedOrdinal() {
            return computed;
        }    }
}
