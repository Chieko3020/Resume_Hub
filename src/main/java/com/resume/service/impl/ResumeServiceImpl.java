package com.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resume.entity.Resume;
import com.resume.entity.User;
import com.resume.mapper.ResumeMapper;
import com.resume.mapper.UserMapper;
import com.resume.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 简历服务实现类
 */
@Service
public class ResumeServiceImpl implements ResumeService {
    @Autowired
    private ResumeMapper resumeMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public Page<Resume> getResumeList(int current, int size) {
        Page<Resume> page = new Page<>(current, size);
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return resumeMapper.selectPage(page, queryWrapper);
    }
    @Override
    public List<Resume> getAllResumes() {
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return resumeMapper.selectList(queryWrapper);
    }
    @Override
    public Resume getResumeById(Integer id) {
        return resumeMapper.selectById(id);
    }
    @Override
    public boolean saveResume(Resume resume) {
        return resumeMapper.insert(resume) > 0;
    }
    @Override
    public boolean updateResume(Resume resume) {
        return resumeMapper.updateById(resume) > 0;
    }
    @Override
    public boolean deleteResume(Integer id) {
        return resumeMapper.deleteById(id) > 0;
    }
    @Override
    public Resume getById(Integer id) {
        return resumeMapper.selectById(id);
    }
    @Override
    public List<Resume> getResumesByUserId(Integer userId) {
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");
        return resumeMapper.selectList(queryWrapper);
    }
    @Override
    public Page<Resume> getResumeListByUsername(int current, int size, String username) {
        Page<Resume> page = new Page<>(current, size);
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            if (user != null) {
                queryWrapper.eq("user_id", user.getId());
            } else {
                // 用户不存在，返回空分页
                return page;
            }
        }
        queryWrapper.orderByDesc("create_time");
        return resumeMapper.selectPage(page, queryWrapper);
    }
}