package com.resume.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;

import java.util.List;

/**
 * 简历服务接口
 */
public interface ResumeService {
    Page<Resume> getResumeList(int current, int size);
    List<Resume> getAllResumes();
    Resume getResumeById(Integer id);
    boolean saveResume(Resume resume);
    boolean updateResume(Resume resume);
    boolean deleteResume(Integer id);
    Resume getById(Integer id);
    List<Resume> getResumesByUserId(Integer userId);
    Page<Resume> getResumeListByUsername(int current, int size, String username);
}