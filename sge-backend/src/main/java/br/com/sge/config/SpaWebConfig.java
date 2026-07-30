package br.com.sge.config;

import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@ConditionalOnProperty(name = "app.spa.enabled", havingValue = "true")
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = super.getResource(resourcePath, location);
                        if (requested != null) {
                            return requested;
                        }
                        if (isApiOrDocsPath(resourcePath)) {
                            return null;
                        }
                        return super.getResource("index.html", location);
                    }
                });
    }

    private static boolean isApiOrDocsPath(String path) {
        return path.startsWith("api/")
                || path.startsWith("v3/")
                || path.startsWith("swagger")
                || path.startsWith("h2-console");
    }
}
