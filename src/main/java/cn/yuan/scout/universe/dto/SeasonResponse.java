package cn.yuan.scout.universe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeasonResponse {

    private Long id;

    private String seasonName;

    private Integer startYear;

    private Integer endYear;

    private Integer status;
}
