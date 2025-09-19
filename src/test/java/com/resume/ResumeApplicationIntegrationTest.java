package com.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.User;
import com.resume.entity.Resume;
import com.resume.dto.ResumeDTO;
import com.resume.service.UserService;
import com.resume.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 简历应用集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("简历应用集成测试")
class ResumeApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeService resumeService;

    private String userToken;
    private String adminToken;
    private User testUser;
    private Resume testResume;

    @BeforeEach
    void setUp() throws Exception {
        // 清理现有数据
        try {
            User existingUser = userService.lambdaQuery().eq(User::getUsername, "testuser").one();
            if (existingUser != null) {
                userService.removeById(existingUser.getId());
            }
            User existingAdmin = userService.lambdaQuery().eq(User::getUsername, "admin").one();
            if (existingAdmin != null) {
                userService.removeById(existingAdmin.getId());
            }
        } catch (Exception e) {
            // 忽略清理错误
        }
        
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole("user");
        userService.save(testUser);

        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("admin");
        userService.save(adminUser);

        // 用户登录获取token
        String loginResponse = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        userToken = objectMapper.readTree(loginResponse)
                .get("data").get("token").asText();

        // 管理员登录获取token
        String adminLoginResponse = mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminToken = objectMapper.readTree(adminLoginResponse)
                .get("data").get("token").asText();
    }

    @Test
    @DisplayName("测试完整的简历管理流程")
    void testCompleteResumeManagementFlow() throws Exception {
        // 1. 创建简历
        ResumeDTO resumeDTO = new ResumeDTO();
        resumeDTO.setName("我的简历");
        resumeDTO.setContent("{\"personalInfo\":{\"name\":\"张三\",\"title\":\"软件工程师\"}}");
        resumeDTO.setThemeId(1);

        String createResponse = mockMvc.perform(post("/api/user/resume")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resumeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 获取创建的简历ID
        Integer resumeId = objectMapper.readTree(createResponse)
                .get("data").get("id").asInt();

        // 2. 获取简历列表
        mockMvc.perform(get("/api/user/resume/list")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("我的简历"));

        // 3. 更新简历
        resumeDTO.setName("更新后的简历");
        resumeDTO.setContent("{\"personalInfo\":{\"name\":\"张三\",\"title\":\"高级软件工程师\"}}");

        mockMvc.perform(put("/api/user/resume/" + resumeId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resumeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 管理员查看所有简历
        mockMvc.perform(get("/api/admin/resume")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("更新后的简历"));

        // 5. 管理员查看用户列表
        mockMvc.perform(get("/api/admin/user/list")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("testuser"));

        // 6. 删除简历
        mockMvc.perform(delete("/api/user/resume/" + resumeId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 7. 验证简历已删除
        mockMvc.perform(get("/api/user/resume/list")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("测试用户注册和登录流程")
    void testUserRegistrationAndLoginFlow() throws Exception {
        // 1. 注册新用户
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword123");
        newUser.setRole("user");

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("注册成功"));

        // 2. 新用户登录
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"newpassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.role").value("user"));

        // 3. 尝试重复注册
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名已存在"));
    }

    @Test
    @DisplayName("测试管理员用户管理功能")
    void testAdminUserManagement() throws Exception {
        // 1. 管理员重置用户密码
        mockMvc.perform(post("/api/admin/user/" + testUser.getId() + "/reset-password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"newpassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("密码重置成功"));

        // 2. 管理员修改用户信息
        User updateUser = new User();
        updateUser.setUsername("updateduser");
        updateUser.setPassword("updatedpassword123");

        mockMvc.perform(put("/api/admin/user/" + testUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("用户信息修改成功"));

        // 3. 验证用户信息已更新
        mockMvc.perform(get("/api/admin/user/list")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("updateduser"));
    }

    @Test
    @DisplayName("测试权限控制")
    void testPermissionControl() throws Exception {
        // 1. 普通用户尝试访问管理员接口
        mockMvc.perform(get("/api/admin/user/list")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("无管理员权限"));

        // 2. 管理员尝试访问用户接口
        mockMvc.perform(get("/api/user/resume/list")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 无token访问受保护接口
        mockMvc.perform(get("/api/user/resume/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }

    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() throws Exception {
        // 1. 无效的JSON格式
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        // 2. 缺少必需字段
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        // 3. 访问不存在的资源
        mockMvc.perform(get("/api/user/resume/999")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("暂无简历数据"));
    }
}
