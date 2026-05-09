package com.canteen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.RechargeRecord;
import com.canteen.entity.User;
import com.canteen.mapper.RechargeRecordMapper;
import com.canteen.mapper.UserMapper;
import com.canteen.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    public Map<String, Object> login(String username, String password) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getStatus, 1));
        
        if (user == null) {
            throw new RuntimeException("用户不存在或已禁用");
        }
        
        if (!MD5Util.md5(password).equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("role", user.getRole());
        result.put("balance", user.getBalance());
        result.put("studentNo", user.getStudentNo());
        
        return result;
    }

    public User register(User user) {
        User existUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (user.getStudentNo() != null) {
            User existStudent = this.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getStudentNo, user.getStudentNo()));
            if (existStudent != null) {
                throw new RuntimeException("学号/工号已绑定");
            }
        }
        
        user.setPassword(MD5Util.md5(user.getPassword()));
        user.setStatus(1);
        user.setBalance(new BigDecimal("0.00"));
        if (user.getRole() == null) {
            user.setRole("student");
        }
        this.save(user);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public User recharge(Long userId, BigDecimal amount) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setBalance(user.getBalance().add(amount));
        this.updateById(user);
        
        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPaymentMethod("online");
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        rechargeRecordMapper.insert(record);
        
        return user;
    }

    public User updateInfo(User user) {
        User existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getName() != null) {
            existUser.setName(user.getName());
        }
        if (user.getPhone() != null) {
            existUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null) {
            existUser.setAvatar(user.getAvatar());
        }
        if (user.getStudentNo() != null) {
            existUser.setStudentNo(user.getStudentNo());
        }
        
        this.updateById(existUser);
        return existUser;
    }
}
