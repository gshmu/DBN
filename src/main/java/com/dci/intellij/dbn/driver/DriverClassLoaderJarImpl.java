package com.dci.intellij.dbn.driver;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Driver;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

@Slf4j
@Getter
public class DriverClassLoaderJarImpl extends ClassLoader implements DriverClassLoader {
    private final File library;
    private final List<Class<Driver>> drivers = new ArrayList<>();
    private final Map<String, byte[]> classData = new HashMap<>();

    @SneakyThrows
    public DriverClassLoaderJarImpl(File library, String location) {
        this.library = library;
        try (JarFile jarFile = new JarFile(library)) {
            List<JarEntry> jarEntries = jarFile.stream().filter(e -> e.getName().startsWith(location) && !e.isDirectory()).collect(Collectors.toList());
            for (JarEntry jarEntry : jarEntries) {
                try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                    load(inputStream, library);
                }

            }
        }
    }


    private void load(InputStream inputStream, File library) throws IOException {
        try (JarInputStream is = new JarInputStream(inputStream)){
            JarEntry jarEntry;
            while ((jarEntry = is.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) continue;
                String entryName = jarEntry.getName();
                int entrySize = (int) jarEntry.getSize();
                if (entrySize == -1) continue;

                byte[] entryData = new byte[entrySize];
                is.read(entryData, 0, entrySize);

                if (entryName.endsWith(".class")) {
                    String className = entryName.replace("/", ".").replace(".class", "");
                    classData.put(className, entryData);

                    try {
                        Class<?> clazz = loadClass(className);
                        if (Driver.class.isAssignableFrom(clazz)) {
                            Class<Driver> driver = cast(clazz);
                            drivers.add(driver);
                        }
                    } catch (Throwable e) {
                        conditionallyLog(e);
                        log.debug("Failed to load driver " + className + " from library " + library, e);
                    }

                }
            }
        }
    }

    public String[] getAllClassNames() {
        Set<String> keyset = classData.keySet();
        return keyset.toArray(new String[0]);
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        // note that it is required to first try loading the class using parent loader
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            conditionallyLog(e);
            return findClass(name);
        }
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        byte[] data = classData.getOrDefault(name, new byte[0]);
        if (data.length == 0) throw new ClassNotFoundException();

        return defineClass(name, data, 0, data.length, null);
    }
}
