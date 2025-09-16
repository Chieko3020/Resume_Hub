package com.resume.controller;

import com.resume.entity.ThemeConfig;
import com.resume.service.ThemeConfigService;
import com.resume.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ThemeController {
    
    @Autowired
    private ThemeConfigService themeConfigService;
    
    /**
     * 管理端：获取所有主题配置
     */
    @GetMapping("/api/admin/themes")
    public Result<List<ThemeConfig>> getAllThemes() {
        List<ThemeConfig> themes = themeConfigService.getAllThemes();
        return Result.success(themes);
    }
    
    /**
     * 用户端：获取所有主题配置（供用户选择主题）
     */
    @GetMapping("/api/user/themes")
    public Result<List<ThemeConfig>> getUserThemes() {
        List<ThemeConfig> themes = themeConfigService.getAllThemes();
        return Result.success(themes);
    }
    
} 