package org.evolve.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import jakarta.annotation.Resource;
import org.evolve.auth.user.service.DataScopeWebInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册 Sa-Token 注解拦截器（启用 @SaCheckPermission 等注解）和数据权限拦截器。
 * </p>
 *
 * @author zhao
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private DataScopeWebInterceptor dataScopeWebInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sa-Token 注解拦截器：启用 @SaCheckPermission / @SaCheckRole 等注解鉴权
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register");

        // 数据权限拦截器：设置当前用户的数据权限上下文
        registry.addInterceptor(dataScopeWebInterceptor)
                .addPathPatterns("/**");
    }
}
