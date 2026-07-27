package com.blogseo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BlogseoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogseoApplication.class, args);
    }
}
