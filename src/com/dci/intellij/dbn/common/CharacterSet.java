package com.dci.intellij.dbn.common;

import javax.swing.Icon;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public class CharacterSet implements Presentable {
    public static List<CharacterSet> ALL = new ArrayList<CharacterSet>();
    static {
        for (Charset charset : Charset.availableCharsets().values()){
            ALL.add(new CharacterSet(charset));
        }

    }

    private Charset charset;
    public CharacterSet(Charset charset) {
        this.charset = charset;
    }

    @NotNull
    @Override
    public String getName() {
        return charset.name();
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    public static CharacterSet get(Charset charset) {
        for (CharacterSet characterSet : ALL) {
            if (characterSet.charset.equals(charset)) {
                return characterSet;
            }
        }
        return null;
    }

    public Charset getCharset() {
        return charset;
    }
}
