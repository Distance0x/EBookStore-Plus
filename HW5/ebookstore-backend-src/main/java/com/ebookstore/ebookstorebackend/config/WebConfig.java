package com.ebookstore.ebookstorebackend.config;
import org.springframework.lang.NonNull;
import com.ebookstore.ebookstorebackend.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Web网络配置
// @NonNull注解用于标记某个参数、方法返回值或字段不能为null
@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")      // 拦截所有API请求
                .excludePathPatterns(            // 排除这些路径
                        "/api/users/login",          // 登录接口
                        "/api/users/check-session"  // session状态检查
                );

       System.out.println("认证拦截器配置完成");
    }
    // 统一在API gateway处理
    // @Override
    // public void addCorsMappings(@NonNull CorsRegistry registry) {
    //     registry.addMapping("/api/**")
    //             .allowedOrigins("http://localhost:5173")  // 前端地址
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true)          // 允许发送cookies
    //             .maxAge(3600);                   // 预检请求缓存时间

    //    System.out.println("CORS配置完成");
    // }
}
