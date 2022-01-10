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

        Masks masks();

        default long maskOn() {
            return masks().on;
        }

        default long maskOff() {
            return masks().off;
        }

        class Masks {
            private final long on;
            private final long off;

            public Masks(LongBase property) {
                int shift = property.ordinal();
                assert shift < 32;
                this.on = 1L << shift;
                this.off = ~this.on;
            }
        }
    }

    interface IntBase extends Property{
        int ordinal();

        Masks masks();

        default int maskOn() {
            return masks().on;
        }

        default int maskOff() {
            return masks().off;
        }

        class Masks {
            private final int on;
            private final int off;

            public Masks(IntBase property) {
                int shift = property.ordinal();
                assert shift < 63;
                this.on = 1 << shift;
                this.off = ~this.on;
           }
        }
    }
}
