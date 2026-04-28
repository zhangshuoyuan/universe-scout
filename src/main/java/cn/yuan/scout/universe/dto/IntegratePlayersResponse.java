package cn.yuan.scout.universe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntegratePlayersResponse {

    private Long seasonId;

    private String seasonName;

    private String templateCode;

    private Integer resultCount;

    private Integer createdCount;

    private Integer updatedCount;

    private Integer historyCount;

    private Integer skippedCount;
}
