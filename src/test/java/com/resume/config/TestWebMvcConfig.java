package com.resume.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 测试环境Web MVC配置类 - 使用测试专用的JWT拦截器
 */
@Configuration
@Primary
public class TestWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TestJwtInterceptor testJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 使用测试专用的JWT拦截器，它不会拦截任何请求
        registry.addInterceptor(testJwtInterceptor)
                .addPathPatterns("/api/admin/**", "/api/user/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将 /admin 和 /admin/ 重定向到 /admin/index.html
        registry.addRedirectViewController("/admin", "/admin/index.html");
        registry.addRedirectViewController("/admin/", "/admin/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        // 确保 /admin/ 路径可以访问到 admin/index.html
        registry.addResourceHandler("/admin/")
                .addResourceLocations("classpath:/static/admin/index.html");
        registry.addResourceHandler("/admin")
                .addResourceLocations("classpath:/static/admin/index.html");
    }
}
