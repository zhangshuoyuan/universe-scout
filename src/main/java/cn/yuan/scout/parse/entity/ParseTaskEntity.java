package cn.yuan.scout.parse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parse_task")
public class ParseTaskEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String taskNo;

    private Long batchId;

    private String templateCode;

    private String status;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
