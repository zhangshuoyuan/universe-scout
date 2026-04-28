package cn.yuan.scout.parse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parse_template")
public class ParseTemplateEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String templateCode;

    private String templateName;

    private String prompt;

    private String jsonSchema;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
