package com.resume.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 简历DTO
 */
@Data
public class ResumeDTO {
    private Integer id;
    @NotBlank(message = "简历名称不能为空")
    private String name;
    @NotBlank(message = "简历内容不能为空")
    private String content;
    private Integer themeId;
}