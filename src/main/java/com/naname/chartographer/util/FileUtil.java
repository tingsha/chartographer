package com.naname.chartographer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class FileUtil {
    private static File databaseAbsolutePath;
    private static File fragmentsAbsolutePath;

    public static void createDirectoriesToSaveData(String path) {
        Path dir = Path.of(path).normalize();
        databaseAbsolutePath = Path.of(dir + File.separator + "database").toFile().getAbsoluteFile();
        fragmentsAbsolutePath = Path.of(dir + File.separator + "fragments").toFile().getAbsoluteFile();

        try {
            Files.createDirectories(databaseAbsolutePath.toPath());
            log.info("Database location: " + databaseAbsolutePath);
            Files.createDirectories(fragmentsAbsolutePath.toPath());
            log.info("Fragments location: " + fragmentsAbsolutePath);
        } catch (IOException e) {
            log.error("Can't create directory cause: " + e.getMessage());
        }
    }

    /**
     * Получить путь к базе данных, если он null, то вернется директория, из которой приложение запущено + '/database'
     */
    public static File getDatabaseAbsolutePath() {
        return databaseAbsolutePath != null ? databaseAbsolutePath : Path.of("database").toFile().getAbsoluteFile();
    }

    /**
     * Получить путь к сохраненным фрагментам, если он null, то вернется директория, из которой приложение запущено
     */
    public static File getFragmentsAbsolutePath() {
        return fragmentsAbsolutePath != null ? fragmentsAbsolutePath : Path.of("").toFile().getAbsoluteFile();
    }
}
