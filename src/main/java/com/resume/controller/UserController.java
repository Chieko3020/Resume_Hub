package com.resume.controller;

import com.resume.entity.User;
import com.resume.entity.Resume;
import com.resume.dto.ResumeDTO;
import com.resume.service.UserService;
import com.resume.service.ResumeService;
import com.resume.util.JwtUtil;
import com.resume.vo.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody @Validated User registerUser) {
        // 检查用户名是否已存在
        User existingUser = userService.lambdaQuery()
            .eq(User::getUsername, registerUser.getUsername())
            .one();
        
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 设置默认角色为普通用户
        registerUser.setRole("user");
        // 加密密码
        String encryptedPassword = com.resume.util.PasswordUtil.encode(registerUser.getPassword());
        registerUser.setPassword(encryptedPassword);
        
        boolean success = userService.save(registerUser);
        if (success) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Validated User loginUser) {
    User user = userService.lambdaQuery()
        .eq(User::getUsername, loginUser.getUsername())
        .one();

    // 调试输出
    System.out.println("输入密码: " + loginUser.getPassword());
    System.out.println("数据库密码: " + (user != null ? user.getPassword() : "null"));
    System.out.println("密码比对结果: " + (user != null ? com.resume.util.PasswordUtil.matches(loginUser.getPassword(), user.getPassword()) : "null"));

        if (user != null && com.resume.util.PasswordUtil.matches(loginUser.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", user.getUsername());
            data.put("role", user.getRole()); // 返回真实角色
            return Result.success("登录成功", data);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 获取当前用户的所有简历
     */
    @GetMapping("/resume/list")
    public Result<List<Resume>> getUserResumes(HttpServletRequest request) {
    Integer userId = getUserIdFromToken(request);
        List<Resume> resumes = resumeService.getResumesByUserId(userId);
        return Result.success(resumes);
    }

    /**
     * 新建简历
     */
    @PostMapping("/resume")
    public Result<?> addResume(@RequestBody @Validated ResumeDTO resumeDTO, HttpServletRequest request) {
    Integer userId = getUserIdFromToken(request);
        Resume resume = new Resume();
        BeanUtils.copyProperties(resumeDTO, resume);
        resume.setUserId(userId);
        resume.setThemeId(resumeDTO.getThemeId());
        // insert 时 id 必须为 null，防止主键冲突
        resume.setId(null);
        boolean success = resumeService.saveResume(resume);
        if (success) {
            return Result.success("简历保存成功", resume);
        }
        return Result.error("简历保存失败");
    }

    /**
     * 编辑简历
     */
    @PutMapping("/resume/{id}")
    public Result<?> updateResume(@PathVariable Integer id, @RequestBody @Validated ResumeDTO resumeDTO, HttpServletRequest request) {
    Integer userId = getUserIdFromToken(request);
        Resume resume = resumeService.getResumeById(id);
        if (resume != null && resume.getUserId().equals(userId)) {
            // 允许更新 name/content/themeId，id 必须为数据库真实主键
            resume.setName(resumeDTO.getName());
            resume.setContent(resumeDTO.getContent());
            resume.setThemeId(resumeDTO.getThemeId());
            boolean success = resumeService.updateResume(resume);
            if (success) {
                return Result.success("简历更新成功");
            }
            return Result.error("简历更新失败");
        }
        return Result.error("无权操作或简历不存在");
    }

    /**
     * 获取当前用户的主简历（首页展示用）
     */
    @GetMapping("/resume")
    public Result<Resume> getMainResume(HttpServletRequest request) {
    Integer userId = getUserIdFromToken(request);
        List<Resume> resumes = resumeService.getResumesByUserId(userId);
        if (resumes != null && !resumes.isEmpty()) {
            // 默认返回第一个简历（如有主简历逻辑可替换）
            return Result.success(resumes.get(0));
        }
        return Result.error("暂无简历数据");
    }

    /**
     * 删除简历
     */
    @DeleteMapping("/resume/{id}")
    public Result<?> deleteResume(@PathVariable Integer id, HttpServletRequest request) {
    Integer userId = getUserIdFromToken(request);
        Resume resume = resumeService.getResumeById(id);
        System.out.println("当前登录userId: " + userId);
        System.out.println("请求删除简历id: " + id);
        System.out.println("查到简历: " + (resume != null ? resume.getId() : "null") + ", userId: " + (resume != null ? resume.getUserId() : "null"));
        if (resume != null && resume.getUserId().equals(userId)) {
            boolean success = resumeService.deleteResume(id);
            if (success) {
                return Result.success("简历删除成功");
            }
            return Result.error("简历删除失败");
        }
        return Result.error("无权操作或简历不存在");
    }


    /**
     * 导出PDF
     */
    @GetMapping("/resume/{id}/export/pdf")
    public void exportPdf(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response) throws IOException, com.itextpdf.text.DocumentException {
        Integer userId = getUserIdFromToken(request);
        Resume resume = resumeService.getResumeById(id);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作或简历不存在");
        }

        // 解析JSON内容获取个人信息
        com.alibaba.fastjson.JSONObject content = com.alibaba.fastjson.JSON.parseObject(resume.getContent());
        com.alibaba.fastjson.JSONObject personalInfo = content.getJSONObject("personalInfo");
        
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
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        try {
            // 使用iText内置中文字体
            com.itextpdf.text.pdf.BaseFont baseFont = com.itextpdf.text.pdf.BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 20, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font subtitleFont = new com.itextpdf.text.Font(baseFont, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.NORMAL);

            // 创建表格用于布局
            com.itextpdf.text.pdf.PdfPTable mainTable = new com.itextpdf.text.pdf.PdfPTable(1);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingAfter(10);

            // 个人信息部分 - 使用表格布局
            com.itextpdf.text.pdf.PdfPTable personalTable = new com.itextpdf.text.pdf.PdfPTable(2);
            personalTable.setWidthPercentage(100);
            personalTable.setSpacingAfter(15);
            
            // 添加个人信息标题
            com.itextpdf.text.pdf.PdfPCell titleCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph(personalInfo.getString("name"), titleFont));
            titleCell.setColspan(2);
            titleCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titleCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            titleCell.setPadding(10);
            personalTable.addCell(titleCell);
            
            // 岗位信息
            com.itextpdf.text.pdf.PdfPCell positionCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph(personalInfo.getString("title"), subtitleFont));
            positionCell.setColspan(2);
            positionCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            positionCell.setPadding(5);
            personalTable.addCell(positionCell);
            
            // 联系方式信息 - 左右分布
            personalTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("姓名：" + personalInfo.getString("name"), normalFont)));
            personalTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("岗位：" + personalInfo.getString("title"), normalFont)));
            personalTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("电话：" + personalInfo.getString("phone"), normalFont)));
            personalTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("邮箱：" + personalInfo.getString("email"), normalFont)));
            personalTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("地址：" + personalInfo.getString("location"), normalFont)));
            personalTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("", normalFont))); // 空单元格
            
            mainTable.addCell(new com.itextpdf.text.pdf.PdfPCell(personalTable));

            // 个人简介部分
            com.itextpdf.text.pdf.PdfPCell summaryCell = new com.itextpdf.text.pdf.PdfPCell();
            summaryCell.addElement(new com.itextpdf.text.Paragraph("个人简介", subtitleFont));
            summaryCell.addElement(new com.itextpdf.text.Paragraph(content.getString("summary"), normalFont));
            summaryCell.setPadding(10);
            summaryCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
            summaryCell.setBorder(com.itextpdf.text.Rectangle.BOX);
            summaryCell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            mainTable.addCell(summaryCell);

            // 教育背景部分
            java.util.List<com.alibaba.fastjson.JSONObject> education = content.getJSONArray("education").toJavaList(com.alibaba.fastjson.JSONObject.class);
            if (!education.isEmpty()) {
                com.itextpdf.text.pdf.PdfPCell educationCell = new com.itextpdf.text.pdf.PdfPCell();
                educationCell.addElement(new com.itextpdf.text.Paragraph("教育背景", subtitleFont));
                for (com.alibaba.fastjson.JSONObject edu : education) {
                    educationCell.addElement(new com.itextpdf.text.Paragraph(edu.getString("school"), normalFont));
                    educationCell.addElement(new com.itextpdf.text.Paragraph(edu.getString("major") + " (" + edu.getString("degree") + ")", smallFont));
                    educationCell.addElement(new com.itextpdf.text.Paragraph(edu.getString("startDate") + " - " + edu.getString("endDate"), smallFont));
                    educationCell.addElement(new com.itextpdf.text.Paragraph(" ")); // 空行
                }
                educationCell.setPadding(10);
                educationCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
                educationCell.setBorder(com.itextpdf.text.Rectangle.BOX);
                educationCell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                mainTable.addCell(educationCell);
            }

            // 工作经历部分
            java.util.List<com.alibaba.fastjson.JSONObject> workExperience = content.getJSONArray("workExperience").toJavaList(com.alibaba.fastjson.JSONObject.class);
            if (!workExperience.isEmpty()) {
                com.itextpdf.text.pdf.PdfPCell workCell = new com.itextpdf.text.pdf.PdfPCell();
                workCell.addElement(new com.itextpdf.text.Paragraph("工作经历", subtitleFont));
                for (com.alibaba.fastjson.JSONObject work : workExperience) {
                    workCell.addElement(new com.itextpdf.text.Paragraph(work.getString("company") + " - " + work.getString("position"), normalFont));
                    workCell.addElement(new com.itextpdf.text.Paragraph(work.getString("startDate") + " - " + work.getString("endDate"), smallFont));
                    workCell.addElement(new com.itextpdf.text.Paragraph(work.getString("description"), normalFont));
                    workCell.addElement(new com.itextpdf.text.Paragraph(" ")); // 空行
                }
                workCell.setPadding(10);
                workCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
                workCell.setBorder(com.itextpdf.text.Rectangle.BOX);
                workCell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                mainTable.addCell(workCell);
            }

            // 项目经历部分
            java.util.List<com.alibaba.fastjson.JSONObject> projectExperience = content.getJSONArray("projectExperience").toJavaList(com.alibaba.fastjson.JSONObject.class);
            if (!projectExperience.isEmpty()) {
                com.itextpdf.text.pdf.PdfPCell projectCell = new com.itextpdf.text.pdf.PdfPCell();
                projectCell.addElement(new com.itextpdf.text.Paragraph("项目经历", subtitleFont));
                for (com.alibaba.fastjson.JSONObject project : projectExperience) {
                    projectCell.addElement(new com.itextpdf.text.Paragraph(project.getString("name") + " - " + project.getString("role"), normalFont));
                    projectCell.addElement(new com.itextpdf.text.Paragraph(project.getString("startDate") + " - " + project.getString("endDate"), smallFont));
                    
                    // 处理技术栈
                    String technologies = "";
                    if (project.getJSONArray("technologies") != null) {
                        technologies = String.join(", ", project.getJSONArray("technologies").toJavaList(String.class));
                    } else if (project.getString("technologies") != null) {
                        technologies = project.getString("technologies");
                    }
                    if (!technologies.isEmpty()) {
                        projectCell.addElement(new com.itextpdf.text.Paragraph("技术栈：" + technologies, smallFont));
                    }
                    
                    projectCell.addElement(new com.itextpdf.text.Paragraph(project.getString("description"), normalFont));
                    projectCell.addElement(new com.itextpdf.text.Paragraph(" ")); // 空行
                }
                projectCell.setPadding(10);
                projectCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
                projectCell.setBorder(com.itextpdf.text.Rectangle.BOX);
                projectCell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                mainTable.addCell(projectCell);
            }

            // 技能特长部分
            java.util.List<String> skills = content.getJSONArray("skills").toJavaList(String.class);
            if (!skills.isEmpty()) {
                com.itextpdf.text.pdf.PdfPCell skillsCell = new com.itextpdf.text.pdf.PdfPCell();
                skillsCell.addElement(new com.itextpdf.text.Paragraph("技能特长", subtitleFont));
                skillsCell.addElement(new com.itextpdf.text.Paragraph(String.join("、", skills), normalFont));
                skillsCell.setPadding(10);
                skillsCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
                skillsCell.setBorder(com.itextpdf.text.Rectangle.BOX);
                skillsCell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                mainTable.addCell(skillsCell);
            }

            // 添加主表格到文档
            document.add(mainTable);
        } finally {
            document.close();
        }
    }


    /**
     * 用户自助重置密码（无需登录）
     */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("password");
        if (username == null || username.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Result.error("用户名和新密码不能为空");
        }
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            return Result.error("用户不存在");
        }
        String encrypted = com.resume.util.PasswordUtil.encode(newPassword);
        user.setPassword(encrypted);
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("密码重置成功");
        }
        return Result.error("密码重置失败");
    }

    /**
     * 从Token中获取当前用户ID
     */
    private Integer getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        
        // 如果Header中没有token，尝试从查询参数获取
        if (token == null || !token.startsWith("Bearer ")) {
            token = request.getParameter("token");
            System.out.println("从查询参数获取token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        } else {
            token = token.substring(7);
            System.out.println("从Header获取token: " + token.substring(0, Math.min(20, token.length())) + "...");
        }
        
        if (token != null && !token.isEmpty()) {
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("从token解析用户名: " + username);
                User user = userService.lambdaQuery().eq(User::getUsername, username).one();
                if (user != null) {
                    System.out.println("找到用户: " + user.getId());
                    return user.getId();
                } else {
                    System.out.println("用户不存在: " + username);
                }
            } catch (Exception e) {
                System.out.println("Token解析失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Token为空");
        }
        throw new RuntimeException("未登录或登录已过期");
    }
}