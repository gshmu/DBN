package com.dci.intellij.dbn.connection.config.tns;

import lombok.Getter;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.util.Lists.convert;
import static com.dci.intellij.dbn.common.util.Lists.filter;

@Getter
public class TnsNamesBundle {
    private final File file;
    private final List<TnsName> profiles;

    public TnsNamesBundle(File file, List<TnsName> profiles) {
        this.file = file;
        this.profiles = profiles;
        Collections.sort(this.profiles);
    }

    public List<String> getProfileNames() {
        return convert(profiles, p -> p.getProfile());
    }

    public List<TnsName> getSelectedProfiles() {
        return filter(profiles, p -> p.isSelected());
    }

    public String getTnsFolder() {
        return file.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
    }
}
