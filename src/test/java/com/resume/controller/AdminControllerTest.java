package com.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.AdminUser;
import com.resume.entity.User;
import com.resume.entity.Resume;
import com.resume.dto.ChangePasswordDTO;
import com.resume.service.AdminUserService;
import com.resume.service.UserService;
import com.resume.service.ResumeService;
import com.resume.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 管理员控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("管理员控制器测试")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private UserService userService;

    @MockBean
    private ResumeService resumeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminUser testAdmin;
    private User testUser;
    private Resume testResume;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testAdmin = new AdminUser();
        testAdmin.setId(1);
        testAdmin.setUsername("admin");
        testAdmin.setPassword("admin123");
        testAdmin.setRole("admin");

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole("user");

        testResume = new Resume();
        testResume.setId(1);
        testResume.setName("测试简历");
        testResume.setUserId(1);
        testResume.setContent("{\"personalInfo\":{\"name\":\"张三\"}}");
        testResume.setThemeId(1);
    }

    @Test
    @DisplayName("测试管理员登录 - 成功")
    void testAdminLoginSuccess() throws Exception {
        // 准备数据
        when(adminUserService.login("admin", "admin123")).thenReturn(testAdmin);
        when(jwtUtil.generateToken("admin", "admin")).thenReturn("admin-token");

        // 执行测试
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.token").value("admin-token"));
    }

    @Test
    @DisplayName("测试管理员登录 - 失败")
    void testAdminLoginFailure() throws Exception {
        // 准备数据
        when(adminUserService.login("admin", "wrongpassword")).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("测试修改管理员密码")
    void testChangePassword() throws Exception {
        // 准备数据
        when(adminUserService.findByUsername("admin")).thenReturn(testAdmin);
        when(adminUserService.changePassword(anyString(), anyString(), anyString())).thenReturn(true);

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("admin123");
        changePasswordDTO.setNewPassword("newpassword123");

        // 执行测试
        mockMvc.perform(post("/api/admin/change-password")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("密码修改成功"));

        verify(adminUserService).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试修改管理员密码 - 旧密码错误")
    void testChangePasswordWrongOldPassword() throws Exception {
        // 准备数据
        when(adminUserService.findByUsername("admin")).thenReturn(testAdmin);
        when(adminUserService.changePassword(anyString(), anyString(), anyString())).thenReturn(false);

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("wrongpassword");
        changePasswordDTO.setNewPassword("newpassword123");

        // 执行测试
        mockMvc.perform(post("/api/admin/change-password")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("密码修改失败"));

        verify(adminUserService).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("测试获取用户列表")
    void testGetUserList() throws Exception {
        // 准备数据
        List<User> users = Arrays.asList(testUser);
        when(userService.list()).thenReturn(users);

        // 执行测试
        mockMvc.perform(get("/api/admin/user/list")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("testuser"));

        verify(userService).list();
    }

    @Test
    @DisplayName("测试重置用户密码")
    void testResetUserPassword() throws Exception {
        // 准备数据
        when(userService.getById(1)).thenReturn(testUser);
        when(userService.updateById(any(User.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/admin/user/1/reset-password")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"newpassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("密码重置成功"));

        verify(userService).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试重置用户密码 - 用户不存在")
    void testResetUserPasswordUserNotFound() throws Exception {
        // 准备数据
        when(userService.getById(999)).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/api/admin/user/999/reset-password")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"newpassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户不存在"));

        verify(userService, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户信息")
    void testUpdateUser() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("admin-token")).thenReturn("admin");
        // 创建User对象用于Mock
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("admin");
        
        when(userService.lambdaQuery().eq(User::getUsername, "admin").eq(User::getRole, "admin").one())
            .thenReturn(adminUser);
        when(userService.getById(1)).thenReturn(testUser);
        when(userService.lambdaQuery().eq(User::getUsername, "newusername").one())
            .thenReturn(null);
        when(userService.updateById(any(User.class))).thenReturn(true);

        User updateUser = new User();
        updateUser.setUsername("newusername");
        updateUser.setPassword("newpassword123");

        // 执行测试
        mockMvc.perform(put("/api/admin/user/1")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("用户信息修改成功"));

        verify(userService).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户信息 - 不能修改自己")
    void testUpdateUserSelf() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("admin-token")).thenReturn("admin");
        // 创建User对象用于Mock
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("admin");
        
        when(userService.lambdaQuery().eq(User::getUsername, "admin").eq(User::getRole, "admin").one())
            .thenReturn(adminUser);

        User updateUser = new User();
        updateUser.setUsername("newusername");
        updateUser.setPassword("newpassword123");

        // 执行测试
        mockMvc.perform(put("/api/admin/user/1")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("不能修改自己，请用修改密码接口"));

        verify(userService, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户信息 - 用户名已存在")
    void testUpdateUserUsernameExists() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("admin-token")).thenReturn("admin");
        // 创建User对象用于Mock
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("admin");
        
        when(userService.lambdaQuery().eq(User::getUsername, "admin").eq(User::getRole, "admin").one())
            .thenReturn(adminUser);
        when(userService.getById(2)).thenReturn(testUser);
        when(userService.lambdaQuery().eq(User::getUsername, "existinguser").one())
            .thenReturn(testUser);

        User updateUser = new User();
        updateUser.setUsername("existinguser");
        updateUser.setPassword("newpassword123");

        // 执行测试
        mockMvc.perform(put("/api/admin/user/2")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名已存在"));

        verify(userService, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试删除用户")
    void testDeleteUser() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("admin-token")).thenReturn("admin");
        // 创建User对象用于Mock
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("admin");
        
        when(userService.lambdaQuery().eq(User::getUsername, "admin").eq(User::getRole, "admin").one())
            .thenReturn(adminUser);
        when(userService.getById(2)).thenReturn(testUser);
        when(resumeService.getResumesByUserId(2)).thenReturn(Arrays.asList(testResume));
        when(resumeService.deleteResume(1)).thenReturn(true);
        when(userService.removeById(2)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/admin/user/2")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("用户删除成功"));

        verify(resumeService).getResumesByUserId(2);
        verify(resumeService).deleteResume(1);
        verify(userService).removeById(2);
    }

    @Test
    @DisplayName("测试删除用户 - 不能删除自己")
    void testDeleteUserSelf() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("admin-token")).thenReturn("admin");
        // 创建User对象用于Mock
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("admin");
        
        when(userService.lambdaQuery().eq(User::getUsername, "admin").eq(User::getRole, "admin").one())
            .thenReturn(adminUser);

        // 执行测试
        mockMvc.perform(delete("/api/admin/user/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("不能删除自己"));

        verify(userService, never()).removeById(anyInt());
    }

    @Test
    @DisplayName("测试获取简历列表")
    void testGetResumeList() throws Exception {
        // 准备数据
        List<Resume> resumes = Arrays.asList(testResume);
        when(resumeService.getAllResumes()).thenReturn(resumes);

        // 执行测试
        mockMvc.perform(get("/api/admin/resume")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("测试简历"));

        verify(resumeService).getAllResumes();
    }

    @Test
    @DisplayName("测试根据ID获取简历")
    void testGetResumeById() throws Exception {
        // 准备数据
        when(resumeService.getResumeById(1)).thenReturn(testResume);

        // 执行测试
        mockMvc.perform(get("/api/admin/resume/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试简历"));

        verify(resumeService).getResumeById(1);
    }

    @Test
    @DisplayName("测试根据ID获取简历 - 简历不存在")
    void testGetResumeByIdNotFound() throws Exception {
        // 准备数据
        when(resumeService.getResumeById(999)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/api/admin/resume/999")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("简历不存在"));

        verify(resumeService).getResumeById(999);
    }

    @Test
    @DisplayName("测试删除简历")
    void testDeleteResume() throws Exception {
        // 准备数据
        when(resumeService.getResumeById(1)).thenReturn(testResume);
        when(resumeService.deleteResume(1)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/admin/resume/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("简历删除成功"));

        verify(resumeService).deleteResume(1);
    }

    @Test
    @DisplayName("测试删除简历 - 简历不存在")
    void testDeleteResumeNotFound() throws Exception {
        // 准备数据
        when(resumeService.getResumeById(999)).thenReturn(null);

        // 执行测试
        mockMvc.perform(delete("/api/admin/resume/999")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("简历不存在"));

        verify(resumeService, never()).deleteResume(anyInt());
    }

    @Test
    @DisplayName("测试删除简历失败")
    void testDeleteResumeFailed() throws Exception {
        // 准备数据
        when(resumeService.getResumeById(1)).thenReturn(testResume);
        when(resumeService.deleteResume(1)).thenReturn(false);

        // 执行测试
        mockMvc.perform(delete("/api/admin/resume/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("简历删除失败"));

        verify(resumeService).deleteResume(1);
    }
}
