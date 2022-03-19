package com.naname.chartographer;

import com.naname.chartographer.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.File;

@Slf4j
@SpringBootApplication
public class ChartographerApplication {

    public static void main(String[] args) {
        if (args.length > 0)
            FileUtil.createDirectoriesToSaveData(args[0]);
        SpringApplication.run(ChartographerApplication.class, args);
    }

    /**
     * Инициализирует базу данных
     */
    @Bean
    public DataSource getDataSource() {
        String url = "jdbc:h2:file:" + FileUtil.getDatabaseAbsolutePath() + File.separator + "chartographer";
        return DataSourceBuilder
                .create()
                .username("root")
                .password("toor")
                .url(url)
                .driverClassName("org.h2.Driver")
                .build();
    }
}
