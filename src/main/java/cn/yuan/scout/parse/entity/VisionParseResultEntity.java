package cn.yuan.scout.parse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vision_parse_result")
public class VisionParseResultEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private Long fileId;

    private String templateCode;

    private String modelName;

    private String requestPrompt;

    private String responseRaw;

    private String responseJson;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
