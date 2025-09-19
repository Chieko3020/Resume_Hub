package com.resume.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resume.entity.Resume;
import com.resume.entity.User;
import com.resume.mapper.ResumeMapper;
import com.resume.mapper.UserMapper;
import com.resume.service.impl.ResumeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 简历服务测试
 */
@DisplayName("简历服务测试")
class ResumeServiceTest {

    @Mock
    private ResumeMapper resumeMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private Resume testResume;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
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
    }

    @Test
    @DisplayName("测试获取简历列表")
    void testGetResumeList() {
        // 准备数据
        Page<Resume> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(testResume));
        
        when(resumeMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
            .thenReturn(expectedPage);

        // 执行测试
        Page<Resume> result = resumeService.getResumeList(1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("测试简历", result.getRecords().get(0).getName());
        
        verify(resumeMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取所有简历")
    void testGetAllResumes() {
        // 准备数据
        List<Resume> expectedResumes = Arrays.asList(testResume);
        when(resumeMapper.selectList(any(QueryWrapper.class)))
            .thenReturn(expectedResumes);

        // 执行测试
        List<Resume> result = resumeService.getAllResumes();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试简历", result.get(0).getName());
        
        verify(resumeMapper).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据ID获取简历")
    void testGetResumeById() {
        // 准备数据
        when(resumeMapper.selectById(1)).thenReturn(testResume);

        // 执行测试
        Resume result = resumeService.getResumeById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试简历", result.getName());
        
        verify(resumeMapper).selectById(1);
    }

    @Test
    @DisplayName("测试根据ID获取简历 - 简历不存在")
    void testGetResumeByIdNotFound() {
        // 准备数据
        when(resumeMapper.selectById(999)).thenReturn(null);

        // 执行测试
        Resume result = resumeService.getResumeById(999);

        // 验证结果
        assertNull(result);
        verify(resumeMapper).selectById(999);
    }

    @Test
    @DisplayName("测试保存简历")
    void testSaveResume() {
        // 准备数据
        when(resumeMapper.insert(any(Resume.class))).thenReturn(1);

        // 执行测试
        boolean result = resumeService.saveResume(testResume);

        // 验证结果
        assertTrue(result);
        verify(resumeMapper).insert(testResume);
    }

    @Test
    @DisplayName("测试保存简历失败")
    void testSaveResumeFailed() {
        // 准备数据
        when(resumeMapper.insert(any(Resume.class))).thenReturn(0);

        // 执行测试
        boolean result = resumeService.saveResume(testResume);

        // 验证结果
        assertFalse(result);
        verify(resumeMapper).insert(testResume);
    }

    @Test
    @DisplayName("测试更新简历")
    void testUpdateResume() {
        // 准备数据
        when(resumeMapper.updateById(any(Resume.class))).thenReturn(1);

        // 执行测试
        boolean result = resumeService.updateResume(testResume);

        // 验证结果
        assertTrue(result);
        verify(resumeMapper).updateById(testResume);
    }

    @Test
    @DisplayName("测试更新简历失败")
    void testUpdateResumeFailed() {
        // 准备数据
        when(resumeMapper.updateById(any(Resume.class))).thenReturn(0);

        // 执行测试
        boolean result = resumeService.updateResume(testResume);

        // 验证结果
        assertFalse(result);
        verify(resumeMapper).updateById(testResume);
    }

    @Test
    @DisplayName("测试删除简历")
    void testDeleteResume() {
        // 准备数据
        when(resumeMapper.deleteById(1)).thenReturn(1);

        // 执行测试
        boolean result = resumeService.deleteResume(1);

        // 验证结果
        assertTrue(result);
        verify(resumeMapper).deleteById(1);
    }

    @Test
    @DisplayName("测试删除简历失败")
    void testDeleteResumeFailed() {
        // 准备数据
        when(resumeMapper.deleteById(1)).thenReturn(0);

        // 执行测试
        boolean result = resumeService.deleteResume(1);

        // 验证结果
        assertFalse(result);
        verify(resumeMapper).deleteById(1);
    }

    @Test
    @DisplayName("测试根据用户ID获取简历")
    void testGetResumesByUserId() {
        // 准备数据
        List<Resume> expectedResumes = Arrays.asList(testResume);
        when(resumeMapper.selectList(any(QueryWrapper.class)))
            .thenReturn(expectedResumes);

        // 执行测试
        List<Resume> result = resumeService.getResumesByUserId(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        
        verify(resumeMapper).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据用户ID获取简历 - 无简历")
    void testGetResumesByUserIdEmpty() {
        // 准备数据
        when(resumeMapper.selectList(any(QueryWrapper.class)))
            .thenReturn(Arrays.asList());

        // 执行测试
        List<Resume> result = resumeService.getResumesByUserId(1);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(resumeMapper).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据用户名获取简历列表 - 用户存在")
    void testGetResumeListByUsernameUserExists() {
        // 准备数据
        Page<Resume> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(testResume));
        
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        when(resumeMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
            .thenReturn(expectedPage);

        // 执行测试
        Page<Resume> result = resumeService.getResumeListByUsername(1, 10, "testuser");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        
        verify(userMapper).selectOne(any(QueryWrapper.class));
        verify(resumeMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据用户名获取简历列表 - 用户不存在")
    void testGetResumeListByUsernameUserNotExists() {
        // 准备数据
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 执行测试
        Page<Resume> result = resumeService.getResumeListByUsername(1, 10, "nonexistent");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
        
        verify(userMapper).selectOne(any(QueryWrapper.class));
        verify(resumeMapper, never()).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据用户名获取简历列表 - 用户名为空")
    void testGetResumeListByUsernameEmpty() {
        // 准备数据
        Page<Resume> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList());
        when(resumeMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
            .thenReturn(expectedPage);

        // 执行测试
        Page<Resume> result = resumeService.getResumeListByUsername(1, 10, "");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
        
        verify(userMapper, never()).selectOne(any(QueryWrapper.class));
        verify(resumeMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据用户名获取简历列表 - 用户名为null")
    void testGetResumeListByUsernameNull() {
        // 准备数据
        Page<Resume> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList());
        when(resumeMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
            .thenReturn(expectedPage);

        // 执行测试
        Page<Resume> result = resumeService.getResumeListByUsername(1, 10, null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
        
        verify(userMapper, never()).selectOne(any(QueryWrapper.class));
        verify(resumeMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }
}
