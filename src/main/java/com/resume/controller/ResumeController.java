package com.resume.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.service.ResumeService;
import com.resume.vo.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 简历控制器
 */
@RestController
@RequestMapping("/api/admin/resume")
public class ResumeController {
    
    @Autowired
    private ResumeService resumeService;
    

    /**
     * 分页查询简历列表（可按用户名过滤）
     */
    @GetMapping("/list")
    public Result<Page<Resume>> getResumeList(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String username) {
        Page<Resume> page;
        if (username != null && !username.isEmpty()) {
            page = resumeService.getResumeListByUsername(current, size, username);
        } else {
            page = resumeService.getResumeList(current, size);
        }
        return Result.success(page);
    }
    
    /**
     * 获取所有简历
     */
    @GetMapping("/all")
    public Result<List<Resume>> getAllResumes() {
        List<Resume> resumes = resumeService.getAllResumes();
        return Result.success(resumes);
    }
    
    /**
     * 根据ID获取简历详情
     */
    @GetMapping("/{id}")
    public Result<Resume> getResumeById(@PathVariable Integer id) {
        Resume resume = resumeService.getResumeById(id);
        if (resume != null) {
            return Result.success(resume);
        }
        return Result.error("简历不存在");
    }
    
    /**
     * 保存简历
     */
    @PostMapping
    public Result<?> saveResume(@RequestBody @Validated ResumeDTO resumeDTO) {
        Resume resume = new Resume();
        BeanUtils.copyProperties(resumeDTO, resume);
        resume.setThemeId(resumeDTO.getThemeId());
        boolean success = resumeService.saveResume(resume);
        if (success) {
            return Result.success("简历保存成功");
        }
        return Result.error("简历保存失败");
    }
    
    /**
     * 更新简历
     */
    @PutMapping("/{id}")
    public Result<?> updateResume(@PathVariable Integer id, @RequestBody @Validated ResumeDTO resumeDTO) {
        Resume resume = resumeService.getResumeById(id);
        if (resume != null) {
            BeanUtils.copyProperties(resumeDTO, resume);
            resume.setId(id);
            resume.setThemeId(resumeDTO.getThemeId());
            boolean success = resumeService.updateResume(resume);
            if (success) {
                return Result.success("简历更新成功");
            }
            return Result.error("简历更新失败");
        }
        return Result.error("简历不存在");
    }
    
    /**
     * 删除简历
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteResume(@PathVariable Integer id) {
        boolean success = resumeService.deleteResume(id);
        if (success) {
            return Result.success("简历删除成功");
        }
        return Result.error("简历删除失败");
    }
    

    @GetMapping("/{id}/export/pdf")
    public void exportPdf(@PathVariable Integer id, HttpServletResponse response) throws IOException, DocumentException {
        Resume resume = resumeService.getById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        // 解析JSON内容获取个人信息
        JSONObject content = JSON.parseObject(resume.getContent());
        JSONObject personalInfo = content.getJSONObject("personalInfo");
        
        // 构建文件名：姓名-岗位.pdf
        String name = personalInfo.getString("name");
        String title = personalInfo.getString("title");
        String fileName = name + "-" + title + ".pdf";
        
        // 对文件名进行URL编码以支持中文
        fileName = java.net.URLEncoder.encode(fileName, "UTF-8");

        // 设置响应头
        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        // 设置为强制下载
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
        // 防止缓存
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 创建PDF文档
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        try {
            // 使用iText内置中文字体
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font subtitleFont = new Font(baseFont, 14, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);

            // 添加个人信息
            document.add(new Paragraph(personalInfo.getString("name"), titleFont));
            document.add(new Paragraph(personalInfo.getString("title"), subtitleFont));
            document.add(new Paragraph("联系方式：" + personalInfo.getString("phone"), normalFont));
            document.add(new Paragraph("邮箱：" + personalInfo.getString("email"), normalFont));
            document.add(new Paragraph("所在地：" + personalInfo.getString("location"), normalFont));
            document.add(new Paragraph("\n"));

            // 添加个人简介
            document.add(new Paragraph("个人简介", subtitleFont));
            document.add(new Paragraph(content.getString("summary"), normalFont));
            document.add(new Paragraph("\n"));

            // 添加教育背景
            document.add(new Paragraph("教育背景", subtitleFont));
            List<JSONObject> education = content.getJSONArray("education").toJavaList(JSONObject.class);
            for (JSONObject edu : education) {
                document.add(new Paragraph(edu.getString("school") + " - " + edu.getString("major"), normalFont));
                document.add(new Paragraph(edu.getString("degree"), normalFont));
                document.add(new Paragraph(edu.getString("startDate") + " 至 " + edu.getString("endDate"), normalFont));
                document.add(new Paragraph("\n"));
            }

            // 添加工作经历
            document.add(new Paragraph("工作经历", subtitleFont));
            List<JSONObject> workExperience = content.getJSONArray("workExperience").toJavaList(JSONObject.class);
            for (JSONObject work : workExperience) {
                document.add(new Paragraph(work.getString("company") + " - " + work.getString("position"), normalFont));
                document.add(new Paragraph(work.getString("startDate") + " 至 " + work.getString("endDate"), normalFont));
                document.add(new Paragraph(work.getString("description"), normalFont));
                document.add(new Paragraph("\n"));
            }

            // 添加项目经历
            document.add(new Paragraph("项目经历", subtitleFont));
            List<JSONObject> projectExperience = content.getJSONArray("projectExperience").toJavaList(JSONObject.class);
            for (JSONObject project : projectExperience) {
                document.add(new Paragraph(project.getString("name") + " - " + project.getString("role"), normalFont));
                document.add(new Paragraph(project.getString("startDate") + " 至 " + project.getString("endDate"), normalFont));
                document.add(new Paragraph("技术栈：" + String.join(", ", project.getJSONArray("technologies").toJavaList(String.class)), normalFont));
                document.add(new Paragraph(project.getString("description"), normalFont));
                document.add(new Paragraph("\n"));
            }

            // 添加技能特长
            document.add(new Paragraph("技能特长", subtitleFont));
            List<String> skills = content.getJSONArray("skills").toJavaList(String.class);
            document.add(new Paragraph(String.join("、", skills), normalFont));
            document.add(new Paragraph("\n"));
        } finally {
            document.close();
        }
    }
} 