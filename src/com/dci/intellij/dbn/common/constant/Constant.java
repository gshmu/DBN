package com.dci.intellij.dbn.common.constant;


import java.util.Objects;

public interface Constant<T extends Constant<T>> {
    default String id() {
        if (this instanceof Enum) {
            Enum enumeration = (Enum) this;
            return enumeration.name();
        }
        throw new AbstractMethodError();
    }

    default boolean is(String id){
        return Objects.equals(id(), id);
    }

    default boolean isOneOf(T... constants){return ConstantUtil.isOneOf(this, constants);}

    static <T> T[] array(T ... constants) {
        return constants;
    }
}
