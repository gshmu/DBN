package com.dci.intellij.dbn.driver;

import com.intellij.openapi.Disposable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Driver;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public class DriverBundle implements Disposable {
    private final DriverClassLoader classLoader;

    public DriverBundle(File library, String location) {
        this.classLoader = new DriverClassLoaderJarImpl(library, location);
    }

    public DriverBundle(File library) {
        this.classLoader = new DriverClassLoaderImpl(library, getClass().getClassLoader());
    }

    @Override
    public void dispose() {
        try {
            classLoader.close();
        } catch (Throwable t) {
            log.warn("Failed to dispose class loader", t);
        }
    }

    @Nullable
    @SneakyThrows
    public Driver getDriver(String className) {
        for (Class<Driver> driver : getDrivers()) {
            if (Objects.equals(driver.getName(), className)) {
                return driver.getDeclaredConstructor().newInstance();
            }
        }
        return null;
    }

    public File getLibrary() {
        return classLoader.getLibrary();
    }

    public List<Class<Driver>> getDrivers() {
        return classLoader.getDrivers();
    }

    public List<File> getJars() {
        return classLoader.getJars();
    }

    public boolean isEmpty() {
        return getDrivers().isEmpty();
    }
}
