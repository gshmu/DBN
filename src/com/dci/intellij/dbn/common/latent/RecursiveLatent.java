package com.dci.intellij.dbn.common.latent;

public class RecursiveLatent<T> extends Latent<T>{
    RecursiveLatent(Loader<T> loader) {
        super(loader);
    }

    @Override
    protected boolean shouldLoad() {
        return !loading && super.shouldLoad() ;
    }
}
