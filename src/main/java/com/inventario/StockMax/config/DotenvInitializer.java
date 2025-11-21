package com.inventario.StockMax.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> props = new HashMap<>();

            for (DotenvEntry entry : dotenv.entries()) {
                props.put(entry.getKey(), entry.getValue());
            }

            // Map common env names to Spring's property names for relaxed binding
            String serverPort = dotenv.get("SERVER_PORT");
            if (serverPort != null && !serverPort.isEmpty()) {
                props.put("server.port", serverPort);
            }

            // If DB_* variables are present, map them to spring.datasource.*
            String host = dotenv.get("DB_HOST");
            String port = dotenv.get("DB_PORT");
            String name = dotenv.get("DB_NAME");
            if (host != null && name != null) {
                if (port == null || port.isEmpty()) port = "3306";
                // Add createDatabaseIfNotExist so local MySQL can be created automatically (requires DB user privileges)
                String jdbc = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true", host, port, name);
                props.put("spring.datasource.url", jdbc);
                props.put("spring.datasource.driverClassName", "com.mysql.cj.jdbc.Driver");
                String user = dotenv.get("DB_USERNAME");
                String pass = dotenv.get("DB_PASSWORD");
                if (user != null) props.put("spring.datasource.username", user);
                if (pass != null) props.put("spring.datasource.password", pass);

                // Ensure a sensible Hibernate dialect is present when using MySQL.
                // If the user set SPRING_JPA_DATABASE_PLATFORM in .env, keep it; otherwise default to a compatible dialect.
                String jpaDialect = dotenv.get("SPRING_JPA_DATABASE_PLATFORM");
                if (jpaDialect == null || jpaDialect.isEmpty()) {
                    props.put("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");
                }
            }

            applicationContext.getEnvironment().getPropertySources().addFirst(new MapPropertySource("dotenvProperties", props));
        } catch (Exception e) {
            throw new ApplicationContextException("Failed to load .env configuration", e);
        }
    }
}
