package cn.yuan.scout.universe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlayerListItemResponse {

    private Long playerId;

    private String playerName;

    private String playerNameCn;

    private String avatarUrl;

    private Long seasonId;

    private String seasonName;

    private Long teamId;

    private String teamName;

    private String teamNameEn;

    private String teamNameCn;

    private String lineupPosition;

    private String position;

    private Integer overallRating;

    private Integer potentialRating;

    private String rosterStatus;

    private String dataJson;

    private String remark;

    private LocalDateTime updatedAt;
}
