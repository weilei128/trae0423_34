package com.canteen.controller;

import com.canteen.common.Result;
import com.canteen.entity.User;
import com.canteen.service.UserService;
import com.canteen.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            
            Map<String, Object> userInfo = userService.login(username, password);
            Long userId = (Long) userInfo.get("userId");
            String role = (String) userInfo.get("role");
            
            String token = JwtUtil.generateToken(userId, username, role);
            userInfo.put("token", token);
            
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            User savedUser = userService.register(user);
            savedUser.setPassword(null);
            return Result.success(savedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            User user = userService.getById(userId);
            if (user != null) {
                user.setPassword(null);
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/recharge")
    public Result<User> recharge(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, BigDecimal> params) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            BigDecimal amount = params.get("amount");
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额无效");
            }
            
            User user = userService.recharge(userId, amount);
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<User> updateInfo(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody User user) {
        try {
            if (token == null) {
                return Result.error(401, "未登录");
            }
            
            Long userId = JwtUtil.getUserId(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error(401, "token无效");
            }
            
            user.setId(userId);
            User updatedUser = userService.updateInfo(user);
            updatedUser.setPassword(null);
            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
