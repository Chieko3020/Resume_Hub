package com.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.User;
import com.resume.entity.Resume;
import com.resume.dto.ResumeDTO;
import com.resume.service.UserService;
import com.resume.service.ResumeService;
import com.resume.util.JwtUtil;
import com.resume.vo.Result;
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
 * 用户控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ResumeService resumeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Resume testResume;
    private ResumeDTO testResumeDTO;

    @BeforeEach
    void setUp() {
        // 准备测试数据
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

        testResumeDTO = new ResumeDTO();
        testResumeDTO.setName("测试简历");
        testResumeDTO.setContent("{\"personalInfo\":{\"name\":\"张三\"}}");
        testResumeDTO.setThemeId(1);
    }

    @Test
    @DisplayName("测试用户注册 - 成功")
    void testRegisterSuccess() throws Exception {
        // 准备数据
        when(userService.lambdaQuery().eq(User::getUsername, "newuser").one())
            .thenReturn(null);
        when(userService.save(any(User.class))).thenReturn(true);

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setRole("user");

        // 执行测试
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("注册成功"));

        verify(userService).save(any(User.class));
    }

    @Test
    @DisplayName("测试用户注册 - 用户名已存在")
    void testRegisterUsernameExists() throws Exception {
        // 准备数据
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);

        // 执行测试
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名已存在"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试用户登录 - 成功")
    void testLoginSuccess() throws Exception {
        // 准备数据
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(jwtUtil.generateToken("testuser", "user"))
            .thenReturn("test-token");

        // 执行测试
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.role").value("user"))
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    @DisplayName("测试用户登录 - 用户名不存在")
    void testLoginUserNotFound() throws Exception {
        // 准备数据
        when(userService.lambdaQuery().eq(User::getUsername, "nonexistent").one())
            .thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("测试用户登录 - 密码错误")
    void testLoginWrongPassword() throws Exception {
        // 准备数据
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);

        // 执行测试
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("测试获取用户简历列表")
    void testGetUserResumes() throws Exception {
        // 准备数据
        List<Resume> resumes = Arrays.asList(testResume);
        when(resumeService.getResumesByUserId(1)).thenReturn(resumes);
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);

        // 执行测试
        mockMvc.perform(get("/api/user/resume/list")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("测试简历"));
    }

    @Test
    @DisplayName("测试获取用户简历列表 - 无简历")
    void testGetUserResumesEmpty() throws Exception {
        // 准备数据
        when(resumeService.getResumesByUserId(1)).thenReturn(Arrays.asList());
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);

        // 执行测试
        mockMvc.perform(get("/api/user/resume/list")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("测试创建简历")
    void testAddResume() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(resumeService.saveResume(any(Resume.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/user/resume")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testResumeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("简历保存成功"));

        verify(resumeService).saveResume(any(Resume.class));
    }

    @Test
    @DisplayName("测试创建简历失败")
    void testAddResumeFailed() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(resumeService.saveResume(any(Resume.class))).thenReturn(false);

        // 执行测试
        mockMvc.perform(post("/api/user/resume")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testResumeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("简历保存失败"));

        verify(resumeService).saveResume(any(Resume.class));
    }

    @Test
    @DisplayName("测试更新简历")
    void testUpdateResume() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(resumeService.getResumeById(1)).thenReturn(testResume);
        when(resumeService.updateResume(any(Resume.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/user/resume/1")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testResumeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("简历更新成功"));

        verify(resumeService).updateResume(any(Resume.class));
    }

    @Test
    @DisplayName("测试更新简历 - 无权限")
    void testUpdateResumeNoPermission() throws Exception {
        // 准备数据
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");
        
        Resume otherResume = new Resume();
        otherResume.setId(1);
        otherResume.setUserId(2); // 不同的用户ID
        
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(resumeService.getResumeById(1)).thenReturn(otherResume);

        // 执行测试
        mockMvc.perform(put("/api/user/resume/1")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testResumeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("无权操作或简历不存在"));

        verify(resumeService, never()).updateResume(any(Resume.class));
    }

    @Test
    @DisplayName("测试删除简历")
    void testDeleteResume() throws Exception {
        // 准备数据
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(resumeService.getResumeById(1)).thenReturn(testResume);
        when(resumeService.deleteResume(1)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/user/resume/1")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("简历删除成功"));

        verify(resumeService).deleteResume(1);
    }

    @Test
    @DisplayName("测试删除简历 - 无权限")
    void testDeleteResumeNoPermission() throws Exception {
        // 准备数据
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");
        
        Resume otherResume = new Resume();
        otherResume.setId(1);
        otherResume.setUserId(2); // 不同的用户ID
        
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);
        when(resumeService.getResumeById(1)).thenReturn(otherResume);

        // 执行测试
        mockMvc.perform(delete("/api/user/resume/1")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("无权操作或简历不存在"));

        verify(resumeService, never()).deleteResume(anyInt());
    }

    @Test
    @DisplayName("测试获取主简历")
    void testGetMainResume() throws Exception {
        // 准备数据
        List<Resume> resumes = Arrays.asList(testResume);
        when(resumeService.getResumesByUserId(1)).thenReturn(resumes);
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);

        // 执行测试
        mockMvc.perform(get("/api/user/resume")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试简历"));
    }

    @Test
    @DisplayName("测试获取主简历 - 无简历")
    void testGetMainResumeEmpty() throws Exception {
        // 准备数据
        when(resumeService.getResumesByUserId(1)).thenReturn(Arrays.asList());
        when(jwtUtil.getUsernameFromToken("test-token")).thenReturn("testuser");
        when(userService.lambdaQuery().eq(User::getUsername, "testuser").one())
            .thenReturn(testUser);

        // 执行测试
        mockMvc.perform(get("/api/user/resume")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("暂无简历数据"));
    }
}
