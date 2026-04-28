package cn.yuan.scout.universe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("player")
public class PlayerEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String playerName;

    private String playerNameCn;

    private String avatarPath;

    private Long seasonId;

    private Long teamId;

    private Long sourceResultId;

    private String lineupPosition;

    private String rosterStatus;

    private String dataJson;

    private String position;

    private Integer overallRating;

    private Integer potentialRating;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
