package cn.yuan.scout.universe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponse {

    private Long id;

    private String teamCode;

    private String teamNameEn;

    private String teamNameCn;
}
