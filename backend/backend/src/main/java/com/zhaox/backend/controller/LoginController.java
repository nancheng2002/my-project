package com.zhaox.backend.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";  // 返回登录页面的HTML
    }

    @GetMapping("/perform_login")
    public String performLogin() {
        // 登录验证和用户授权逻辑
        return "Successfully logged in";  // 返回成功信息
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext(); // 清除上下文
        return "Logged out successfully";
    }
}
