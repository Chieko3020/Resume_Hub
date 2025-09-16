package com.resume.controller;

import com.resume.dto.ChangePasswordDTO;
import com.resume.dto.LoginDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.resume.entity.AdminUser;
import com.resume.entity.User;
import com.resume.entity.Resume;
import com.resume.service.AdminUserService;
import com.resume.service.UserService;
import com.resume.service.ResumeService;
import com.resume.util.JwtUtil;
import com.resume.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeService resumeService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Validated LoginDTO loginDTO) {
        // 1) 优先走 admin_user 表（兼容旧表结构）
        AdminUser adminUser = adminUserService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (adminUser != null) {
            String token = jwtUtil.generateToken(adminUser.getUsername(), "admin");
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", adminUser.getUsername());
            return Result.success("登录成功", data);
        }

        // 2) 退化到 user 表（role=admin）
        User user = userService.lambdaQuery().eq(User::getUsername, loginDTO.getUsername()).eq(User::getRole, "admin").one();
        if (user != null && com.resume.util.PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername(), "admin");
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", user.getUsername());
            return Result.success("登录成功", data);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<?> changePassword(@RequestBody @Validated ChangePasswordDTO changePasswordDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            boolean success = adminUserService.changePassword(username, changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
            if (success) {
                return Result.success("密码修改成功");
            }
            return Result.error("旧密码错误");
        }
        return Result.error("未登录或登录已过期");
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/user/list")
    public Result<List<?>> getUserList() {
        // 直接返回 user 表（因为 admin_user 表不存在）
        try {
            List<User> users = userService.list();
            return Result.success(users);
        } catch (Exception e) {
            // 如果 user 表也有问题，返回空列表
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 修改用户名/密码
     */
    @PutMapping("/user/{id}")
    public Result<?> updateUser(@PathVariable Integer id, @RequestBody AdminUser updateUser, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String currentUsername = jwtUtil.getUsernameFromToken(token.substring(7));
        
        // 检查是否是修改自己
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", currentUsername).eq("role", "admin");
        User currentUser = userService.getOne(queryWrapper);
        if (currentUser != null && currentUser.getId().equals(id)) {
            return Result.error("不能修改自己，请用修改密码接口");
        }
        
        // 直接更新 user 表
        User u = userService.getById(id);
        if (u == null) {
            return Result.error("用户不存在");
        }
        
        // 检查用户名是否已存在（如果要修改用户名）
        if (updateUser.getUsername() != null && !updateUser.getUsername().equals(u.getUsername())) {
            QueryWrapper<User> usernameQuery = new QueryWrapper<>();
            usernameQuery.eq("username", updateUser.getUsername());
            User existingUser = userService.getOne(usernameQuery);
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }
            u.setUsername(updateUser.getUsername());
        }
        
        // 更新密码
        if (updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()) {
            String encrypted = com.resume.util.PasswordUtil.encode(updateUser.getPassword());
            u.setPassword(encrypted);
        }
        
        boolean ok = userService.updateById(u);
        if (ok) return Result.success("用户信息修改成功");
        return Result.error("用户信息修改失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String currentUsername = jwtUtil.getUsernameFromToken(token.substring(7));
        
        // 检查是否是删除自己
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", currentUsername).eq("role", "admin");
        User currentUser = userService.getOne(queryWrapper);
        if (currentUser != null && currentUser.getId().equals(id)) {
            return Result.error("不能删除自己");
        }
        
        // 删除用户关联的所有简历
        try {
            List<Resume> userResumes = resumeService.getResumesByUserId(id);
            for (Resume resume : userResumes) {
                resumeService.deleteResume(resume.getId());
            }
        } catch (Exception e) {
            // 如果删除简历失败，记录日志但不阻止用户删除
            System.err.println("删除用户简历时出错: " + e.getMessage());
        }
        
        // 删除用户
        boolean ok = userService.removeById(id);
        if (ok) return Result.success("用户删除成功");
        return Result.error("用户删除失败");
    }

    /**
     * 重置用户密码（管理员操作）
     */
    @PostMapping("/user/{id}/reset-password")
    public Result<?> resetUserPassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        String encrypted = com.resume.util.PasswordUtil.encode(newPassword);
        
        // 直接使用 user 表
        User u = userService.getById(id);
        if (u == null) {
            return Result.error("用户不存在");
        }
        u.setPassword(encrypted);
        boolean ok = userService.updateById(u);
        if (ok) return Result.success("密码重置成功");
        return Result.error("密码重置失败");
    }
}