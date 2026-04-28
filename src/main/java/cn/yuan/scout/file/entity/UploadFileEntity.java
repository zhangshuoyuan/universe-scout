package cn.yuan.scout.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_file")
public class UploadFileEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private String originalFilename;

    private String fileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Integer imageWidth;

    private Integer imageHeight;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;

    private Integer deleted;
}
