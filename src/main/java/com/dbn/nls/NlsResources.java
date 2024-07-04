package com.dbn.nls;

import com.dbn.common.util.Named;
import com.intellij.DynamicBundle;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;

import static com.dbn.common.util.Commons.nvl;

public class NlsResources extends DynamicBundle{
    public static final @NonNls String BUNDLE = "nls.NlsResources";
    private static final NlsResources INSTANCE = new NlsResources();

    private NlsResources() {
        //DynamicBundle.getResourceBundle(BUNDLE, NlsResources.class.getClassLoader());
        super(BUNDLE);
    }

    public static @Nls String nls(@NonNls @PropertyKey(resourceBundle = BUNDLE) String key, Object ... params) {
        adjustParams(params);
        return INSTANCE.getMessage(key, params);
    }

    private static void adjustParams(Object ... params) {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Exception) {
                Exception exception = (Exception) params[i];
                params[i] = nvl(
                        exception.getLocalizedMessage(),
                        exception.getClass().getSimpleName());
            } else if (params[i] instanceof Named) {
                Named named = (Named) params[i];
                params[i] = named.getName();
            } else if (params[i] instanceof File) {
                File file = (File) params[i];
                params[i] = file.getPath();
            } else if (params[i] instanceof VirtualFile) {
                VirtualFile file = (VirtualFile) params[i];
                params[i] = file.getPath();
            }
        }
    }
}
