package com.zhaox.backend.controller;

import com.zhaox.backend.common.Result;
import com.zhaox.backend.dto.LoginRequest;
import com.zhaox.backend.entity.User;
import com.zhaox.backend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录接口，传递用户名和密码
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Result.fail("用户名和密码不能为空");
        }

        User user = userService.authenticateUser(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());

        return Result.ok(data);
    }
}
