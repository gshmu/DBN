package com.dci.intellij.dbn.common.property;

public interface Property {
    @Deprecated
    default PropertyGroup group() {
        return null;
    }

    default boolean implicit() {
        return false;
    }

    interface LongBase extends Property{
        int ordinal();

        Computed computedOrdinal();

        default long computedZero() {
            return computedOrdinal().zero;
        }

        default long computedOne() {
            return computedOrdinal().one;
        }

        class Computed {
            private final long zero;
            private final long one;

            public Computed(LongBase p) {
                int shift = p.ordinal() + 1;
                this.zero = ~(1L << shift);
                this.one = 1L << shift;
            }
        }
    }

    interface IntBase extends Property{
        int ordinal();

        Computed computedOrdinal();

        default int computedZero() {
            return computedOrdinal().zero;
        }

        default int computedOne() {
            return computedOrdinal().one;
        }

        class Computed {
            private final int zero;
            private final int one;

            public Computed(IntBase p) {
                int shift = p.ordinal() + 1;
                this.zero = ~(1 << shift);
                this.one = 1 << shift;
            }
        }
    }
}
