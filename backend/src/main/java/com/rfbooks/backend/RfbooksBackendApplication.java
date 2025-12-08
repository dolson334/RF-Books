package com.rfbooks.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class RfbooksBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RfbooksBackendApplication.class, args);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> hibernateProperties.put("hibernate.jdbc.lob.non_contextual_creation", true);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("*");
            }
        };
    }
}