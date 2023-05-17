package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.util.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Presentable extends Named {
    @NotNull
    String getName();

    @Nullable
    default String getDescription() {return null;}

    @Nullable
    default Icon getIcon() {return null;}

    Presentable UNKNOWN = new Presentable() {
        @NotNull
        @Override
        public String getName() {
            return "Unknown";
        }

        @Nullable
        @Override
        public String getDescription() {
            return null;
        }

        @Nullable
        @Override
        public Icon getIcon() {
            return null;
        }
    };

    static Presentable basic(String name) {
        return new Presentable() {
            @NotNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Presentable) {
                    Presentable presentable = (Presentable) obj;
                    return Objects.equals(name, presentable.getName());
                }
                return false;
            }
        };
    }

    static List<Presentable> basic(Collection<String> names) {
        return names.stream().map(n -> basic(n)).collect(Collectors.toList());
    }
}
