package com.resume.controller;

import com.resume.entity.ThemeConfig;
import com.resume.service.ThemeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/theme")
public class ThemeConfigController {
    @Autowired
    private ThemeConfigService themeConfigService;

    @GetMapping("/list")
    public List<ThemeConfig> list() {
        return themeConfigService.list();
    }

    @GetMapping("/{id}")
    public ThemeConfig getById(@PathVariable Integer id) {
        return themeConfigService.getById(id);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody ThemeConfig themeConfig) {
        return themeConfigService.save(themeConfig);
    }

    @PutMapping("/update")
    public boolean update(@RequestBody ThemeConfig themeConfig) {
        return themeConfigService.updateById(themeConfig);
    }

    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Integer id) {
        return themeConfigService.removeById(id);
    }
}
