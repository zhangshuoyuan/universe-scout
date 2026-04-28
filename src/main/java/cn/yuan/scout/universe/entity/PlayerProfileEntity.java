package cn.yuan.scout.universe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("player_profile")
public class PlayerProfileEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String playerKey;

    private String playerName;

    private String playerNameCn;

    private String avatarPath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
