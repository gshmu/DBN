package com.dci.intellij.dbn.common.latent;

public class RecursiveLatent<T> extends Latent<T>{
    private boolean loading;

    RecursiveLatent(Loader<T> loader) {
        super(loader);
    }

    @Override
    protected boolean shouldLoad() {
        return !loading && super.shouldLoad() ;
    }

    @Override
    protected void loading() {
        super.loading();
        loading = true;
    }

    @Override
    public void loaded(T value) {
        super.loaded(value);
        loading = false;
    }

    @Override
    public void reset() {
        super.reset();
        loading = false;
    }
}
