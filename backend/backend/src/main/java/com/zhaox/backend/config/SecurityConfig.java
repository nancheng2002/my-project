package com.zhaox.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()  // 禁用 CSRF 防护
                .authorizeRequests()
                .requestMatchers("/login", "/register").permitAll()  // 替代 antMatchers 使用 requestMatchers
                .anyRequest().authenticated()  // 其他请求需要认证
                .and()
                .formLogin()  // 启用表单登录
                .loginPage("/login")  // 登录页面 URL
                .loginProcessingUrl("/perform_login")  // 提交表单 URL
                .defaultSuccessUrl("/home", true);  // 登录成功后的默认页面

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 使用 BCrypt 进行密码加密
    }
}
