package com.dci.intellij.dbn.common.property;

public interface Property{
    long index();

    default PropertyGroup group(){return null;}

    default boolean implicit(){return false;}

    static long idx(Enum property) {
        double idx = Math.pow(2, property.ordinal());
        if (idx > Long.MAX_VALUE) {
            throw new IllegalArgumentException("");
        }

        return (long) idx;
    }
}
