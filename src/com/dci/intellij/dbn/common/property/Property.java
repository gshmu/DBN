package com.dci.intellij.dbn.common.property;

public interface Property{
    int ordinal();

    Computed computedOrdinal();

    default long computedZero(){return computedOrdinal().zero;};

    default long computedOne() {return computedOrdinal().one;}

    default PropertyGroup group(){return null;}

    default boolean implicit(){return false;}

    class Computed {
        private final long zero;
        private final long one;

        public Computed(Property p) {
            this.zero = ~(1L << p.ordinal());
            this.one = 1L << p.ordinal();
        }
    }
}
