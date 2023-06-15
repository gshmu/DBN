package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Slf4j
@Getter
class DriverClassLoaderImpl extends URLClassLoader implements DriverClassLoader {
    private File library;
    private final List<File> jars = new ArrayList<>();
    private final List<Class<Driver>> drivers = new ArrayList<>();

    public DriverClassLoaderImpl(File library, ClassLoader parent) {
        super(getUrls(library), parent);
        this.library = library;
        load();
    }

    @Deprecated
    public DriverClassLoaderImpl(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }


    @SneakyThrows
    private void load() {
        ProgressMonitor.setProgressText("Loading jdbc drivers from " + library);
        URL[] urls = getURLs();
        for (URL url : urls) {
            load(new File(url.toURI()));
        }
    }

    private void load(File jar) {
        jars.add(jar);
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.replaceAll("/", "\\.");
                    className = className.substring(0, className.length() - 6);
                    try {
                        Class<?> clazz = loadClass(className);
                        if (Driver.class.isAssignableFrom(clazz)) {
                            Class<Driver> driver = cast(clazz);
                            drivers.add(driver);
                        }
                    } catch (Throwable e) {
                        conditionallyLog(e);
                        log.debug("Failed to load driver " + className + " from library " + jar, e);
                    }
                }
            }
        } catch (Throwable e) {
            conditionallyLog(e);
            log.debug("Failed to load drivers from library " + jar, e);
        }
    }

    @SneakyThrows
    private static URL[] getUrls(File library) {
        if (library.isDirectory()) {
            File[] files = library.listFiles();
            if (files == null || files.length == 0) throw new IOException("No fiels found at location");
            return Arrays.
                    stream(files).
                    filter(file -> file.getName().endsWith(".jar")).
                    map(file -> getFileUrl(file)).
                    toArray(URL[]::new);
        } else {
            return new URL[]{getFileUrl(library)};
        }
    }

    @SneakyThrows
    private static URL getFileUrl(File file) {
        return file.toURI().toURL();
    }

}
