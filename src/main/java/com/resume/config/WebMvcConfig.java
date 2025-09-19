package com.resume.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173") // 只允许前端开发地址
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/admin/**", "/api/user/**") // 拦截所有用户和管理端接口
                .excludePathPatterns("/api/admin/login", "/api/user/login"); // 排除登录接口
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