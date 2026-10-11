package com.example.securespringinspector;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot entry point. The application intentionally runs as a CLI rather
 * than starting a web server.
 */
@SpringBootApplication
public class SecureSpringInspectorApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SecureSpringInspectorApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        ConfigurableApplicationContext context = application.run(args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }

    @Bean
    ExitCodeGenerator exitCodeGenerator(com.example.securespringinspector.cli.InspectorCommandLineRunner runner) {
        return runner::getExitCode;
    }

}
