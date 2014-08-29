package com.dci.intellij.dbn.language.common.element.parser;

public class Branch {
    String name;
    double version = 0;

    public Branch(String def) {
        int index = def.indexOf("@");
        if (index > -1) {
            name = def.substring(0, index);
            version = Double.parseDouble(def.substring(index + 1));
        } else {
            name = def;
        }

    }

    public String getName() {
        return name;
    }

    public double getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return name.equals(branch.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + "@" + version;
    }
}