package com.dci.intellij.dbn.common.property;

public interface Property{
    int index();

    default PropertyGroup group(){return null;}

    default boolean implicit(){return false;}

    static int idx(Enum property) {
        //return INDEX.getPrime(property.ordinal());
        double pow = Math.pow(2, property.ordinal());
        if (pow > Integer.MAX_VALUE) {
            System.out.println(pow);
        }
        return (int) pow;

    }
}
