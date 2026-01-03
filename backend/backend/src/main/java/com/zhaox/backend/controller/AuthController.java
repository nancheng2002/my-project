package com.zhaox.backend.controller;

import com.zhaox.backend.entity.User;
import com.zhaox.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 登录接口，传递用户名和密码
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // 调用 UserService 的认证方法，验证用户名和密码
        User user = userService.authenticateUser(username, password);

        // 如果用户存在，返回登录成功的消息
        if (user != null) {
            return "登录成功";
        }

        // 如果用户名或密码错误，返回错误信息
        return "用户名或密码错误";
    }
}
