package com.dci.intellij.dbn.common.property;

public interface Property{
    int ordinal();

    default PropertyGroup group(){return null;}

    default boolean implicit(){return false;}
}
