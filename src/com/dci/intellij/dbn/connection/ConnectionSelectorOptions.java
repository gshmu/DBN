package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class ConnectionSelectorOptions extends PropertyHolderBase.IntStore<ConnectionSelectorOptions.Option> {

    @Override
    protected Option[] properties() {
        return Option.values();
    }

    public enum Option implements Property.IntBase {
        SHOW_VIRTUAL_CONNECTIONS,
        SHOW_CREATE_CONNECTION,
        PROMPT_SCHEMA_SELECTION;

        private final Masks masks = new Masks(this);

        @Override
        public Masks masks() {
            return masks;
        }    }
}
