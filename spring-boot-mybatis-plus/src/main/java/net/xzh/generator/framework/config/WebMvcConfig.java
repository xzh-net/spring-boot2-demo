package net.xzh.generator.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            String trimmedSource = source.trim();
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(trimmedSource, formatter);
                } catch (DateTimeParseException e) {
                }
            }
            throw new IllegalArgumentException("Cannot parse date: " + source);
        });

        registry.addConverter(String.class, java.time.LocalDate.class, source -> {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            return java.time.LocalDate.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        });
    }
}