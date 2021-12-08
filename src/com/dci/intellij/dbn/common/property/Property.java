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

        Computed computed();

        default long computedOn() {
            return computed().on;
        }

        default long computedOff() {
            return computed().off;
        }

        class Computed {
            private final long on;
            private final long off;

            public Computed(LongBase p) {
                int shift = p.ordinal() + 1;
                this.on = 1L << shift;
                this.off = ~(1L << shift);
            }
        }
    }

    interface IntBase extends Property{
        int ordinal();

        Computed computed();

        default int computedOn() {
            return computed().on;
        }

        default int computedOff() {
            return computed().off;
        }

        class Computed {
            private final int on;
            private final int off;

            public Computed(IntBase p) {
                int shift = p.ordinal() + 1;
                this.on = 1 << shift;
                this.off = ~(1 << shift);
                assert shift < 32;
            }
        }
    }
}
