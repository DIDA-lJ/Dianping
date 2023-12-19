package com.linqi.config;

import com.linqi.utils.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author linqi
 * @version 1.0.0
 * @description 用户全文拦截器
 */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type",
                        "/upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                );

    }
}
