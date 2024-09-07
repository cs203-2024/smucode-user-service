package com.cs203.smucode.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ComponentScan(basePackages = {
    "com.cs203.smucode.controllers",
    "com.cs203.smucode.services",
    "com.cs203.smucode.repositories"
})
public class ProjectConfig {
    
}

