package br.com.gimvpark.web.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GimvProps props;
    private final AuthInterceptor auth;

    public WebConfig(GimvProps props, AuthInterceptor auth) {
        this.props = props;
        this.auth = auth;
    }

    @Override
    public void addInterceptors(InterceptorRegistry r) {
        r.addInterceptor(auth).addPathPatterns("/", "/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry r) {
        String dir = Path.of(props.uploadDir()).toAbsolutePath().toUri().toString();
        r.addResourceHandler("/uploads/**").addResourceLocations(dir);
    }
}
