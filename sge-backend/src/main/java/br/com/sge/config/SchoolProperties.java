package br.com.sge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.school")
public record SchoolProperties(String packageId) {}
