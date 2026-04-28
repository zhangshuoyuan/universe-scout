package cn.yuan.scout.parse.controller;

import cn.yuan.scout.common.exception.BizException;
import cn.yuan.scout.common.exception.ErrorCode;
import cn.yuan.scout.common.result.Result;
import cn.yuan.scout.parse.dto.PromptTemplateResponse;
import cn.yuan.scout.parse.dto.UpdatePromptTemplateRequest;
import cn.yuan.scout.parse.entity.ParseTemplateEntity;
import cn.yuan.scout.parse.service.ParseTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/parse/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final ParseTemplateService parseTemplateService;
    private final PromptTemplateJsonValidator jsonValidator;

    @GetMapping
    public Result<List<PromptTemplateResponse>> listTemplates() {
        List<PromptTemplateResponse> records = parseTemplateService.list(
                        new LambdaQueryWrapper<ParseTemplateEntity>()
                                .eq(ParseTemplateEntity::getDeleted, 0)
                                .orderByAsc(ParseTemplateEntity::getCreatedAt)
                )
                .stream()
                .map(this::toResponse)
                .toList();
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<PromptTemplateResponse> getTemplate(@PathVariable Long id) {
        ParseTemplateEntity template = getExistingTemplate(id);
        return Result.success(toResponse(template));
    }

    @PutMapping("/{id}")
    public Result<PromptTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromptTemplateRequest request
    ) {
        ParseTemplateEntity template = getExistingTemplate(id);
        String jsonSchema = jsonValidator.normalize(request.getJsonSchema());

        boolean updated = parseTemplateService.update(
                new LambdaUpdateWrapper<ParseTemplateEntity>()
                        .eq(ParseTemplateEntity::getId, id)
                        .eq(ParseTemplateEntity::getDeleted, 0)
                        .set(ParseTemplateEntity::getTemplateName, request.getTemplateName().trim())
                        .set(ParseTemplateEntity::getPrompt, request.getPrompt().trim())
                        .set(ParseTemplateEntity::getJsonSchema, jsonSchema)
                        .set(ParseTemplateEntity::getStatus, request.getStatus())
                        .set(ParseTemplateEntity::getUpdatedAt, LocalDateTime.now())
        );
        if (!updated) {
            throw new BizException(ErrorCode.PARAM_ERROR, "提示词模板更新失败");
        }

        template = getExistingTemplate(template.getId());
        return Result.success(template == null ? null : toResponse(template));
    }

    private ParseTemplateEntity getExistingTemplate(Long id) {
        ParseTemplateEntity template = parseTemplateService.getOne(
                new LambdaQueryWrapper<ParseTemplateEntity>()
                        .eq(ParseTemplateEntity::getId, id)
                        .eq(ParseTemplateEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (template == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "提示词模板不存在");
        }
        return template;
    }

    private PromptTemplateResponse toResponse(ParseTemplateEntity template) {
        return PromptTemplateResponse.builder()
                .id(template.getId())
                .templateCode(template.getTemplateCode())
                .templateName(template.getTemplateName())
                .prompt(template.getPrompt())
                .jsonSchema(template.getJsonSchema())
                .status(template.getStatus())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    @Component
    @RequiredArgsConstructor
    static class PromptTemplateJsonValidator {

        private final ObjectMapper objectMapper;

        String normalize(String jsonSchema) {
            if (!StringUtils.hasText(jsonSchema)) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(objectMapper.readTree(jsonSchema));
            } catch (Exception e) {
                throw new BizException(ErrorCode.PARAM_ERROR, "JSON Schema 必须是合法 JSON");
            }
        }
    }
}
