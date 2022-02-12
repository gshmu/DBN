package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolderBase;

import java.util.Arrays;

public class ConnectionSelectorOptions extends PropertyHolderBase.IntStore<ConnectionSelectorOptions.Option> {

    @Override
    protected Option[] properties() {
        return Option.values();
    }

    public enum Option implements Property.IntBase {
        SHOW_VIRTUAL_CONNECTIONS,
        SHOW_CREATE_CONNECTION,
        PROMPT_SCHEMA_SELECTION;

        private final IntMasks masks = new IntMasks(this);

        @Override
        public IntMasks masks() {
            return masks;
        }
    }

    public static ConnectionSelectorOptions options(Option ... options) {
        ConnectionSelectorOptions holder = new ConnectionSelectorOptions();
        Arrays.stream(options).forEach(option -> holder.set(option));
        return holder;
    }

}
