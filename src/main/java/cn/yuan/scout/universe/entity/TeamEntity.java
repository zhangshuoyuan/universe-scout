package cn.yuan.scout.universe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("team")
public class TeamEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String teamCode;

    private String teamNameEn;

    private String teamNameCn;

    private String city;

    private String conference;

    private String division;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
