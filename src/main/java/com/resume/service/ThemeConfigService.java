
package com.resume.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.resume.entity.ThemeConfig;
import java.util.List;

/**
 * 主题服务接口
 */
public interface ThemeConfigService extends IService<ThemeConfig> {
    /**
     * 获取所有主题配置
     */
    List<ThemeConfig> getAllThemes();

}