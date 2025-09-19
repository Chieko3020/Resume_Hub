package com.resume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.resume.entity.ThemeConfig;
import com.resume.mapper.ThemeConfigMapper;
import com.resume.service.ThemeConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThemeConfigServiceImpl extends ServiceImpl<ThemeConfigMapper, ThemeConfig> implements ThemeConfigService {
    
    @Override
    public List<ThemeConfig> getAllThemes() {
        return list();
    }
    
} 