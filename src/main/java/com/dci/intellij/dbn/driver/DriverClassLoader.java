package com.dci.intellij.dbn.driver;

import java.net.URL;
import java.net.URLClassLoader;

public class DriverClassLoader extends URLClassLoader {
    public DriverClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
