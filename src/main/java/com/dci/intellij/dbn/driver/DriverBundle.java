package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.intellij.openapi.Disposable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.sql.Driver;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Slf4j
@Getter
public class DriverBundle implements Disposable {
    private DriverClassLoader classLoader;
    private final File library;
    private final List<File> jars = new ArrayList<>();
    private final List<Class<Driver>> drivers = new ArrayList<>();

    public DriverBundle(File library) {
        this.library = library;
        load();
    }

    private void load() {
        ProgressMonitor.setProgressText("Loading jdbc drivers from " + library);
        ClassLoader parentClassLoader = getClass().getClassLoader();
        if (library.isDirectory()) {
            File[] files = library.listFiles();
            if (files != null) {
                URL[] urls = Arrays.
                        stream(files).
                        filter(file -> file.getName().endsWith(".jar")).
                        map(file -> getFileUrl(file)).
                        toArray(URL[]::new);

                classLoader = new DriverClassLoader(urls, parentClassLoader);
                for (File file : files) {
                    load(file);
                }
            }
        } else {
            URL[] urls = new URL[]{getFileUrl(library)};
            classLoader = new DriverClassLoader(urls, parentClassLoader);
            load(library);
        }

    }

    private void load(File libraryFile) {
        try (JarFile jarFile = new JarFile(libraryFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.replaceAll("/", "\\.");
                    className = className.substring(0, className.length() - 6);
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (Driver.class.isAssignableFrom(clazz)) {
                            Class<Driver> driver = cast(clazz);
                            drivers.add(driver);
                        }
                    } catch (Throwable t) {
                        log.debug("Failed to load driver " + className + " from library " + libraryFile, t);
                    }
                }
            }
        } catch (Throwable t) {
            log.debug("Failed to load drivers from library " + libraryFile, t);
        }
    }

    @Override
    public void dispose() {
        try {
            if (!drivers.isEmpty()) {
                classLoader.close();
                drivers.clear();
                jars.clear();
            }
        } catch (Throwable t) {
            log.warn("Failed to dispose class loader", t);
        }
    }

    @Nullable
    @SneakyThrows
    public Driver getDriver(String className) {
        for (Class<Driver> driver : drivers) {
            if (Objects.equals(driver.getName(), className)) {
                return driver.getDeclaredConstructor().newInstance();
            }
        }
        return null;
    }



    @NotNull
    @SneakyThrows
    private static URL getFileUrl(File file) {
        return file.toURI().toURL();
    }

    public boolean isEmpty() {
        return drivers.isEmpty();
    }
}
