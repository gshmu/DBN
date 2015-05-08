package com.dci.intellij.dbn.common.ui;

public class OptionBundle<T extends Option> {
    private T[] options;

    public OptionBundle(T[] options) {
        this.options = options;
    }

    public boolean is(T option) {
        if (options != null) {
            for (T opt : options) {
                if (opt == option) {
                    return true;
                }
            }
        }
        return false;
    }
}
