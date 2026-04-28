package cn.yuan.scout.universe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlayerProfileResponse {

    private Long profileId;

    private String playerKey;

    private String playerName;

    private String playerNameCn;

    private String avatarUrl;

    private String position;

    private LocalDateTime updatedAt;
}
