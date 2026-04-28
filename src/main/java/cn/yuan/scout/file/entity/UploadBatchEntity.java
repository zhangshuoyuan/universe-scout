package cn.yuan.scout.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_batch")
public class UploadBatchEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String batchNo;

    private String batchName;

    private String sourceType;

    private Long seasonId;

    private String status;

    private Integer totalFiles;

    private Integer successCount;

    private Integer failedCount;

    private String remark;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
