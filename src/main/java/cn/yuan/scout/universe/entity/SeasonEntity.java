package cn.yuan.scout.universe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("season")
public class SeasonEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String seasonName;

    private Integer startYear;

    private Integer endYear;

    private String description;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
