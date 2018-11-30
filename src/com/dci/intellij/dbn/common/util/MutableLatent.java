package com.dci.intellij.dbn.common.util;


public class MutableLatent<T, M> extends Latent<T> {
    private M identifier;
    private UncheckedLoader<M> mutable;

    private MutableLatent(Loader<T> loader, UncheckedLoader<M> mutable) {
        super(loader);
        this.mutable = mutable;
    }

    public static <T, M> MutableLatent<T, M> create(UncheckedLoader<M> mutable, Loader<T> loader) {
        return new MutableLatent<T, M>(loader, mutable);
    }

    @Override
    protected boolean shouldLoad() {
        return super.shouldLoad() ||
                identifier == null ||
                !identifier.equals(mutable.load());
    }

    @Override
    protected void initLoad() {
        identifier = mutable.load();
    }

    @FunctionalInterface
    public interface UncheckedLoader<T> {
        T load();
    }
}
